package org.dragberry.eshop.dal.repo;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dragberry.eshop.dal.sort.Roots;
import org.dragberry.eshop.dal.sort.SortConfig;
import org.dragberry.eshop.dal.sort.SortContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class AbstractSearchQuery <T, R extends Roots> {
	
	private static final Pattern SORT_PATTERN = Pattern.compile("^(.*?)\\[(.*?)\\]$");
	
	private static final String SORT_PARAM = "sort";
	
	private static final String FROM_PARAM = "from[{0}]";
	
	private static final String TO_PARAM = "to[{0}]";
	
	private static final String ATTRIBUTE_PARAM = "attribute[{0}]";
	
	protected final EntityManager entityManager;
	protected final CriteriaBuilder cb;
	protected final CriteriaQuery<T> query;
	protected final CriteriaQuery<Long> countQuery;
	
	protected R roots;
    protected R countRoots;
	
	public AbstractSearchQuery(Class<T> resultClass, EntityManager entityManager) {
		this.entityManager = entityManager;
		this.cb = this.entityManager.getCriteriaBuilder();
		this.query = this.cb.createQuery(resultClass);
		this.countQuery = this.cb.createQuery(Long.class);
		this.roots = getRoots(query);
		this.countRoots = getRoots(countQuery);
	}
	
	protected abstract R getRoots(CriteriaQuery<?> query);
	
	public Page<T> search(Pageable pageRequest, Map<String, String[]> searchParams) {
		countQuery.select(cb.countDistinct(getCountExpression()));
		where(searchParams, countRoots).ifPresent(countQuery::where);
		Long total = entityManager.createQuery(countQuery).getSingleResult();
		
		query.distinct(true).multiselect(getSelectionList());
        where(searchParams, roots).ifPresent(query::where);
        orderBy(searchParams).ifPresent(query::orderBy); 
        groupBy(roots).ifPresent(query::groupBy);
		return new PageImpl<>(entityManager.createQuery(query)
				.setFirstResult(pageRequest.getPageNumber() * pageRequest.getPageSize())
				.setMaxResults(pageRequest.getPageSize())
				.getResultList(), pageRequest, total);
	}
	
    protected abstract List<Selection<?>> getSelectionList();
    
    protected abstract Expression<?> getCountExpression();
	
    protected Optional<Predicate> where(Map<String, String[]> searchParams, R roots) {
        List<Predicate> predicates = new ArrayList<>();
        where(predicates, searchParams, roots);
        return predicates.isEmpty() ? Optional.empty() : Optional.of(cb.and(predicates.toArray(new Predicate[predicates.size()])));
    }
    
	protected abstract void where(List<Predicate> predicates, Map<String, String[]> searchParams, R roots);
	
	protected Optional<Order> orderBy(Map<String, String[]> searchParams) {
		String[] values = searchParams.get(SORT_PARAM);
		if (ArrayUtils.getLength(values) > 0) {
			Matcher m = SORT_PATTERN.matcher(values[0]);
			if (m.matches()) {
				return getSortConfig().get(m.group(1)).getOrder(getSortContext(), m.group(2));
			}
		}
		return getSortConfig().getDefault().getOrder(getSortContext());
	}
	
	protected Optional<List<Expression<?>>> groupBy(R roots) {
        return Optional.empty();
	}
	
	protected abstract SortConfig<R> getSortConfig();
	
	protected SortContext<R> getSortContext() {
        return SortContext.of(cb, roots);
    }
	
	protected List<Predicate> numericRange(String param, Path<BigDecimal> path, Map<String, String[]> searchParams) {
		List<Predicate> predicates = new ArrayList<>(2);
		String[] from = searchParams.get(MessageFormat.format(FROM_PARAM, param));
		if (ArrayUtils.isNotEmpty(from) && NumberUtils.isCreatable(from[0])) {
			predicates.add(cb.greaterThanOrEqualTo(path, new BigDecimal(from[0])));
		}
		String[] to = searchParams.get(MessageFormat.format(TO_PARAM, param));
		if (ArrayUtils.isNotEmpty(to) && NumberUtils.isCreatable(to[0])) {
			predicates.add(cb.lessThanOrEqualTo(path, new BigDecimal(to[0])));
		}
		return predicates;
	}
	
	protected List<Predicate> dateRange(String param, Path<LocalDateTime> path, Map<String, String[]> searchParams) {
		List<Predicate> predicates = new ArrayList<>(2);
		String[] from = searchParams.get(MessageFormat.format(FROM_PARAM, param));
		if (ArrayUtils.isNotEmpty(from)) {
			try {
				LocalDateTime fromDate = LocalDateTime.parse(from[0]);
				predicates.add(cb.greaterThanOrEqualTo(path, fromDate));
			} catch (DateTimeParseException exc) {
 				log.warn("Unable to parse date " + from[0]);
			}
		}
		String[] to = searchParams.get(MessageFormat.format(TO_PARAM, param));
		if (ArrayUtils.isNotEmpty(to)) {
			try {
				LocalDateTime toDate = LocalDateTime.parse(to[0]);
				predicates.add(cb.lessThanOrEqualTo(path, toDate));
			} catch (DateTimeParseException exc) {
				log.warn("Unable to parse date " + to[0]);
			}
		}
		return predicates;
	}
	
	protected <E> List<Predicate> in(String param, Path<E> path, Map<String, String[]> searchParams, java.util.function.Predicate<String> isValid, Function<String, E> mapper) {
        String[] values = searchParams.get(MessageFormat.format(ATTRIBUTE_PARAM, param));
        if (ArrayUtils.isNotEmpty(values)) {
            return Arrays.asList(path.in(Arrays.stream(values)
                    .filter(isValid).map(mapper).collect(Collectors.toList())));
        }
        return Collections.emptyList();
    }
	
	protected <E extends Enum<E>> List<Predicate> inEnum(String param, Path<E> path, Class<E> clazz, Map<String, String[]> searchParams) {
        return in(param, path, searchParams, value -> EnumUtils.isValidEnum(clazz, value), value -> EnumUtils.getEnum(clazz, value));
    }
	
	protected List<Predicate> inLong(String param, Path<Long> path, Map<String, String[]> searchParams) {
	    return in(param, path, searchParams, NumberUtils::isCreatable, Long::valueOf);
	}
	
	protected List<Predicate> inBoolean(String param, Path<Boolean> path, Map<String, String[]> searchParams) {
        return in(param, path, searchParams,  val -> true, Boolean::parseBoolean);
    }
	
	protected List<Predicate> likeFromString(String paramName, Map<String, String[]> searchParams, List<Path<String>> paths) {
	    String[] queryStr = searchParams.get(paramName);
        if (ArrayUtils.isNotEmpty(queryStr) && !queryStr[0].isEmpty()) {
    	    return Arrays.stream(queryStr[0].toUpperCase().split("\\s+"))
                    .map(str -> "%" + str +  "%")
                    .flatMap(param -> paths.stream().map(path -> cb.like(cb.upper(path), param)))
                    .collect(Collectors.toList());
        }
        return List.of();
	}
}