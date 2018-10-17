/* on page init for mobiles */
$(document).on('pageinit', function() {
	onPageResize();
	calculateRatings();
	updateCommentsCount();
});
/* On documnet ready */
$(document).ready(function() {
	onPageResize();
	$(window).resize(onPageResize);
	calculateRatings();
	updateCommentsCount();
	$(window).scroll(function(obj) {
	 	console.log(obj);
	});
});

$(document).ajaxError(showServerErrorModal);

/*
 * This method should be used as success ajax function when server can return validation errors
 */
function processResult(result, success) {
	if (result.issues.length === 0) {
		success(result.value);
	} else {
		showValidationErrors(result.issues);
	}
}

/* Show server validation messages */
function showValidationErrors(issues) {
	issues.forEach(function(issue) {
		$fieldId = $('#' + issue.fieldId).addClass('is-invalid');
		$fieldId.after($('<label></label>')
			.attr('id', $fieldId.attr('id') + '-error')
			.attr('for', $fieldId.attr('id'))
			.addClass('invalid-feedback d-block')
			.text(issue.message));
	});
}

/* Update body margin on screen resize */
function onPageResize() {
	var bodyMargin = $('body').css('margin-bottom');
	var footerHeight = $('footer').height();
	if (bodyMargin + 50 != footerHeight) {
		$("body").css('margin-bottom', footerHeight + 50);
	}
}
/* Calculate ratings */
function calculateRatings() {
	$('.star-rating').each(function() {
		$stars = $(this).find('.fa-star');
		$stars.each(function(){
			if (Math.round($stars.siblings('input.rating-value').val()) >= parseInt($(this).data('rating'))) {
		      return $(this).addClass('active');
		    } else {
		      return $(this).removeClass('active');
			}
		});
	});
}

/* Builds title for comments count */
$.fn.buildCommentTitle = function(num, suffix) {
  return num == 0 ? 'Нет отзывов' : num + ' ' + suffix;
}

/* Builds title for comments count */
$.fn.getCommentTitle = function(num) {
  var str = '' + num;
  if (str.endsWith('10') || str.endsWith('11') || str.endsWith('12') || str.endsWith('14')) {
   return $.fn.buildCommentTitle(num, 'отзывов');
  }
  if (str.endsWith('1')) {
   return $.fn.buildCommentTitle(num, 'отзыв');
  }
  if (str.endsWith('2') || str.endsWith('3') || str.endsWith('4')) {
   return $.fn.buildCommentTitle(num, 'отзыва');
  }
  return $.fn.buildCommentTitle(num, 'отзывов');
}

/* Updates comments count */
function updateCommentsCount() {
	$('.product-comments-count').each(function() {
		$(this).text($.fn.getCommentTitle($(this).data('value')));
	});
}

/* Show an error model window */
function showServerErrorModal() {
	$('#serverErrorModal').modal('show');
}

/**
 * Refresh cart count when the product is added to cart
 */
function refreshCartCount(cartState) {
	$(".cart-product-count").text(cartState.quantity == 0 ? '' : cartState.quantity);
}

/**
 * Performs a quick search with  delay
 */
var delaySearchTimer;
function doSearch() {
	clearTimeout(delaySearchTimer);
	delaySearchTimer = setTimeout(function() {
		var form = $('#searchForm');
	    $.ajax({
	    	type: 'GET',
	        url: quickSearchUrl,
	        data: form.serialize(),
	        success: function(data)  {
        		if ($('#searchResults').hasClass('show')) {
        			$('#searchResultsTrigger ').dropdown('toggle');				        			
        		}
    			$('#searchResults').html(data.value);
        		if (!$('#searchResults').hasClass('show')) {
        			$('#searchResultsTrigger ').dropdown('toggle');				        			
        		}
        		console.log(data);
	        }
	   	});
	}, 250);
}
