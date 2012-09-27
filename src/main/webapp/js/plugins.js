// Avoid `console` errors in browsers that lack a console.
if (!(window.console && console.log)) {
	(function() {
		var noop = function() {};
		var methods = ['assert', 'clear', 'count', 'debug', 'dir', 'dirxml', 'error', 'exception', 'group', 'groupCollapsed', 'groupEnd', 'info', 'log', 'markTimeline', 'profile', 'profileEnd', 'markTimeline', 'table', 'time', 'timeEnd', 'timeStamp', 'trace', 'warn'];
		var length = methods.length;
		var console = window.console = {};
		while (length--) {
			console[methods[length]] = noop;
		}
	}());
}

/**
 * Isotope v1.5.19
 * An exquisite jQuery plugin for magical layouts
 * http://isotope.metafizzy.co
 *
 * Commercial use requires one-time license fee
 * http://metafizzy.co/#licenses
 *
 * Copyright 2012 David DeSandro / Metafizzy
 */

/*jshint asi: true, browser: true, curly: true, eqeqeq: true, forin: false, immed: false, newcap: true, noempty: true, strict: true, undef: true */
/*global jQuery: false */

(function( window, $, undefined ){

	'use strict';

	// get global vars
	var document = window.document;
	var Modernizr = window.Modernizr;

	// helper function
	var capitalize = function( str ) {
		return str.charAt(0).toUpperCase() + str.slice(1);
	};

	// ========================= getStyleProperty by kangax ===============================
	// http://perfectionkills.com/feature-testing-css-properties/

	var prefixes = 'Moz Webkit O Ms'.split(' ');

	var getStyleProperty = function( propName ) {
		var style = document.documentElement.style,
				prefixed;

		// test standard property first
		if ( typeof style[propName] === 'string' ) {
			return propName;
		}

		// capitalize
		propName = capitalize( propName );

		// test vendor specific properties
		for ( var i=0, len = prefixes.length; i < len; i++ ) {
			prefixed = prefixes[i] + propName;
			if ( typeof style[ prefixed ] === 'string' ) {
				return prefixed;
			}
		}
	};

	var transformProp = getStyleProperty('transform'),
			transitionProp = getStyleProperty('transitionProperty');


	// ========================= miniModernizr ===============================
	// <3<3<3 and thanks to Faruk and Paul for doing the heavy lifting

	/*!
	 * Modernizr v1.6ish: miniModernizr for Isotope
	 * http://www.modernizr.com
	 *
	 * Developed by:
	 * - Faruk Ates  http://farukat.es/
	 * - Paul Irish  http://paulirish.com/
	 *
	 * Copyright (c) 2009-2010
	 * Dual-licensed under the BSD or MIT licenses.
	 * http://www.modernizr.com/license/
	 */

	/*
	 * This version whittles down the script just to check support for
	 * CSS transitions, transforms, and 3D transforms.
	*/

	var tests = {
		csstransforms: function() {
			return !!transformProp;
		},

		csstransforms3d: function() {
			var test = !!getStyleProperty('perspective');
			// double check for Chrome's false positive
			if ( test ) {
				var vendorCSSPrefixes = ' -o- -moz- -ms- -webkit- -khtml- '.split(' '),
						mediaQuery = '@media (' + vendorCSSPrefixes.join('transform-3d),(') + 'modernizr)',
						$style = $('<style>' + mediaQuery + '{#modernizr{height:3px}}' + '</style>')
												.appendTo('head'),
						$div = $('<div id="modernizr" />').appendTo('html');

				test = $div.height() === 3;

				$div.remove();
				$style.remove();
			}
			return test;
		},

		csstransitions: function() {
			return !!transitionProp;
		}
	};

	var testName;

	if ( Modernizr ) {
		// if there's a previous Modernzir, check if there are necessary tests
		for ( testName in tests) {
			if ( !Modernizr.hasOwnProperty( testName ) ) {
				// if test hasn't been run, use addTest to run it
				Modernizr.addTest( testName, tests[ testName ] );
			}
		}
	} else {
		// or create new mini Modernizr that just has the 3 tests
		Modernizr = window.Modernizr = {
			_version : '1.6ish: miniModernizr for Isotope'
		};

		var classes = ' ';
		var result;

		// Run through tests
		for ( testName in tests) {
			result = tests[ testName ]();
			Modernizr[ testName ] = result;
			classes += ' ' + ( result ?  '' : 'no-' ) + testName;
		}

		// Add the new classes to the <html> element.
		$('html').addClass( classes );
	}


	// ========================= isoTransform ===============================

	/**
	 *  provides hooks for .css({ scale: value, translate: [x, y] })
	 *  Progressively enhanced CSS transforms
	 *  Uses hardware accelerated 3D transforms for Safari
	 *  or falls back to 2D transforms.
	 */

	if ( Modernizr.csstransforms ) {

				// i.e. transformFnNotations.scale(0.5) >> 'scale3d( 0.5, 0.5, 1)'
		var transformFnNotations = Modernizr.csstransforms3d ?
			{ // 3D transform functions
				translate : function ( position ) {
					return 'translate3d(' + position[0] + 'px, ' + position[1] + 'px, 0) ';
				},
				scale : function ( scale ) {
					return 'scale3d(' + scale + ', ' + scale + ', 1) ';
				}
			} :
			{ // 2D transform functions
				translate : function ( position ) {
					return 'translate(' + position[0] + 'px, ' + position[1] + 'px) ';
				},
				scale : function ( scale ) {
					return 'scale(' + scale + ') ';
				}
			}
		;

		var setIsoTransform = function ( elem, name, value ) {
					// unpack current transform data
			var data =  $.data( elem, 'isoTransform' ) || {},
					newData = {},
					fnName,
					transformObj = {},
					transformValue;

			// i.e. newData.scale = 0.5
			newData[ name ] = value;
			// extend new value over current data
			$.extend( data, newData );

			for ( fnName in data ) {
				transformValue = data[ fnName ];
				transformObj[ fnName ] = transformFnNotations[ fnName ]( transformValue );
			}

			// get proper order
			// ideally, we could loop through this give an array, but since we only have
			// a couple transforms we're keeping track of, we'll do it like so
			var translateFn = transformObj.translate || '',
					scaleFn = transformObj.scale || '',
					// sorting so translate always comes first
					valueFns = translateFn + scaleFn;

			// set data back in elem
			$.data( elem, 'isoTransform', data );

			// set name to vendor specific property
			elem.style[ transformProp ] = valueFns;
		};

		// ==================== scale ===================

		$.cssNumber.scale = true;

		$.cssHooks.scale = {
			set: function( elem, value ) {
				// uncomment this bit if you want to properly parse strings
				// if ( typeof value === 'string' ) {
				//   value = parseFloat( value );
				// }
				setIsoTransform( elem, 'scale', value );
			},
			get: function( elem, computed ) {
				var transform = $.data( elem, 'isoTransform' );
				return transform && transform.scale ? transform.scale : 1;
			}
		};

		$.fx.step.scale = function( fx ) {
			$.cssHooks.scale.set( fx.elem, fx.now+fx.unit );
		};


		// ==================== translate ===================

		$.cssNumber.translate = true;

		$.cssHooks.translate = {
			set: function( elem, value ) {

				// uncomment this bit if you want to properly parse strings
				// if ( typeof value === 'string' ) {
				//   value = value.split(' ');
				// }
				//
				// var i, val;
				// for ( i = 0; i < 2; i++ ) {
				//   val = value[i];
				//   if ( typeof val === 'string' ) {
				//     val = parseInt( val );
				//   }
				// }

				setIsoTransform( elem, 'translate', value );
			},

			get: function( elem, computed ) {
				var transform = $.data( elem, 'isoTransform' );
				return transform && transform.translate ? transform.translate : [ 0, 0 ];
			}
		};

	}

	// ========================= get transition-end event ===============================
	var transitionEndEvent, transitionDurProp;

	if ( Modernizr.csstransitions ) {
		transitionEndEvent = {
			WebkitTransitionProperty: 'webkitTransitionEnd',  // webkit
			MozTransitionProperty: 'transitionend',
			OTransitionProperty: 'oTransitionEnd',
			transitionProperty: 'transitionEnd'
		}[ transitionProp ];

		transitionDurProp = getStyleProperty('transitionDuration');
	}

	// ========================= smartresize ===============================

	/*
	 * smartresize: debounced resize event for jQuery
	 *
	 * latest version and complete README available on Github:
	 * https://github.com/louisremi/jquery.smartresize.js
	 *
	 * Copyright 2011 @louis_remi
	 * Licensed under the MIT license.
	 */

	var $event = $.event,
			resizeTimeout;

	$event.special.smartresize = {
		setup: function() {
			$(this).bind( "resize", $event.special.smartresize.handler );
		},
		teardown: function() {
			$(this).unbind( "resize", $event.special.smartresize.handler );
		},
		handler: function( event, execAsap ) {
			// Save the context
			var context = this,
					args = arguments;

			// set correct event type
			event.type = "smartresize";

			if ( resizeTimeout ) { clearTimeout( resizeTimeout ); }
			resizeTimeout = setTimeout(function() {
				jQuery.event.handle.apply( context, args );
			}, execAsap === "execAsap"? 0 : 100 );
		}
	};

	$.fn.smartresize = function( fn ) {
		return fn ? this.bind( "smartresize", fn ) : this.trigger( "smartresize", ["execAsap"] );
	};



// ========================= Isotope ===============================


	// our "Widget" object constructor
	$.Isotope = function( options, element, callback ){
		this.element = $( element );

		this._create( options );
		this._init( callback );
	};

	// styles of container element we want to keep track of
	var isoContainerStyles = [ 'width', 'height' ];

	var $window = $(window);

	$.Isotope.settings = {
		resizable: true,
		layoutMode : 'masonry',
		containerClass : 'isotope',
		itemClass : 'isotope-item',
		hiddenClass : 'isotope-hidden',
		hiddenStyle: { opacity: 0, scale: 0.001 },
		visibleStyle: { opacity: 1, scale: 1 },
		containerStyle: {
			position: 'relative',
			overflow: 'hidden'
		},
		animationEngine: 'best-available',
		animationOptions: {
			queue: false,
			duration: 800
		},
		sortBy : 'original-order',
		sortAscending : true,
		resizesContainer : true,
		transformsEnabled: !$.browser.opera, // disable transforms in Opera
		itemPositionDataEnabled: false
	};

	$.Isotope.prototype = {

		// sets up widget
		_create : function( options ) {

			this.options = $.extend( {}, $.Isotope.settings, options );

			this.styleQueue = [];
			this.elemCount = 0;

			// get original styles in case we re-apply them in .destroy()
			var elemStyle = this.element[0].style;
			this.originalStyle = {};
			// keep track of container styles
			var containerStyles = isoContainerStyles.slice(0);
			for ( var prop in this.options.containerStyle ) {
				containerStyles.push( prop );
			}
			for ( var i=0, len = containerStyles.length; i < len; i++ ) {
				prop = containerStyles[i];
				this.originalStyle[ prop ] = elemStyle[ prop ] || '';
			}
			// apply container style from options
			this.element.css( this.options.containerStyle );

			this._updateAnimationEngine();
			this._updateUsingTransforms();

			// sorting
			var originalOrderSorter = {
				'original-order' : function( $elem, instance ) {
					instance.elemCount ++;
					return instance.elemCount;
				},
				random : function() {
					return Math.random();
				}
			};

			this.options.getSortData = $.extend( this.options.getSortData, originalOrderSorter );

			// need to get atoms
			this.reloadItems();

			// get top left position of where the bricks should be
			this.offset = {
				left: parseInt( ( this.element.css('padding-left') || 0 ), 10 ),
				top: parseInt( ( this.element.css('padding-top') || 0 ), 10 )
			};

			// add isotope class first time around
			var instance = this;
			setTimeout( function() {
				instance.element.addClass( instance.options.containerClass );
			}, 0 );

			// bind resize method
			if ( this.options.resizable ) {
				$window.bind( 'smartresize.isotope', function() {
					instance.resize();
				});
			}

			// dismiss all click events from hidden events
			this.element.delegate( '.' + this.options.hiddenClass, 'click', function(){
				return false;
			});

		},

		_getAtoms : function( $elems ) {
			var selector = this.options.itemSelector,
					// filter & find
					$atoms = selector ? $elems.filter( selector ).add( $elems.find( selector ) ) : $elems,
					// base style for atoms
					atomStyle = { position: 'absolute' };

			if ( this.usingTransforms ) {
				atomStyle.left = 0;
				atomStyle.top = 0;
			}

			$atoms.css( atomStyle ).addClass( this.options.itemClass );

			this.updateSortData( $atoms, true );

			return $atoms;
		},

		// _init fires when your instance is first created
		// (from the constructor above), and when you
		// attempt to initialize the widget again (by the bridge)
		// after it has already been initialized.
		_init : function( callback ) {

			this.$filteredAtoms = this._filter( this.$allAtoms );
			this._sort();
			this.reLayout( callback );

		},

		option : function( opts ){
			// change options AFTER initialization:
			// signature: $('#foo').bar({ cool:false });
			if ( $.isPlainObject( opts ) ){
				this.options = $.extend( true, this.options, opts );

				// trigger _updateOptionName if it exists
				var updateOptionFn;
				for ( var optionName in opts ) {
					updateOptionFn = '_update' + capitalize( optionName );
					if ( this[ updateOptionFn ] ) {
						this[ updateOptionFn ]();
					}
				}
			}
		},

		// ====================== updaters ====================== //
		// kind of like setters

		_updateAnimationEngine : function() {
			var animationEngine = this.options.animationEngine.toLowerCase().replace( /[ _\-]/g, '');
			var isUsingJQueryAnimation;
			// set applyStyleFnName
			switch ( animationEngine ) {
				case 'css' :
				case 'none' :
					isUsingJQueryAnimation = false;
					break;
				case 'jquery' :
					isUsingJQueryAnimation = true;
					break;
				default : // best available
					isUsingJQueryAnimation = !Modernizr.csstransitions;
			}
			this.isUsingJQueryAnimation = isUsingJQueryAnimation;
			this._updateUsingTransforms();
		},

		_updateTransformsEnabled : function() {
			this._updateUsingTransforms();
		},

		_updateUsingTransforms : function() {
			var usingTransforms = this.usingTransforms = this.options.transformsEnabled &&
				Modernizr.csstransforms && Modernizr.csstransitions && !this.isUsingJQueryAnimation;

			// prevent scales when transforms are disabled
			if ( !usingTransforms ) {
				delete this.options.hiddenStyle.scale;
				delete this.options.visibleStyle.scale;
			}

			this.getPositionStyles = usingTransforms ? this._translate : this._positionAbs;
		},


		// ====================== Filtering ======================

		_filter : function( $atoms ) {
			var filter = this.options.filter === '' ? '*' : this.options.filter;

			if ( !filter ) {
				return $atoms;
			}

			var hiddenClass    = this.options.hiddenClass,
					hiddenSelector = '.' + hiddenClass,
					$hiddenAtoms   = $atoms.filter( hiddenSelector ),
					$atomsToShow   = $hiddenAtoms;

			if ( filter !== '*' ) {
				$atomsToShow = $hiddenAtoms.filter( filter );
				var $atomsToHide = $atoms.not( hiddenSelector ).not( filter ).addClass( hiddenClass );
				this.styleQueue.push({ $el: $atomsToHide, style: this.options.hiddenStyle });
			}

			this.styleQueue.push({ $el: $atomsToShow, style: this.options.visibleStyle });
			$atomsToShow.removeClass( hiddenClass );

			return $atoms.filter( filter );
		},

		// ====================== Sorting ======================

		updateSortData : function( $atoms, isIncrementingElemCount ) {
			var instance = this,
					getSortData = this.options.getSortData,
					$this, sortData;
			$atoms.each(function(){
				$this = $(this);
				sortData = {};
				// get value for sort data based on fn( $elem ) passed in
				for ( var key in getSortData ) {
					if ( !isIncrementingElemCount && key === 'original-order' ) {
						// keep original order original
						sortData[ key ] = $.data( this, 'isotope-sort-data' )[ key ];
					} else {
						sortData[ key ] = getSortData[ key ]( $this, instance );
					}
				}
				// apply sort data to element
				$.data( this, 'isotope-sort-data', sortData );
			});
		},

		// used on all the filtered atoms
		_sort : function() {

			var sortBy = this.options.sortBy,
					getSorter = this._getSorter,
					sortDir = this.options.sortAscending ? 1 : -1,
					sortFn = function( alpha, beta ) {
						var a = getSorter( alpha, sortBy ),
								b = getSorter( beta, sortBy );
						// fall back to original order if data matches
						if ( a === b && sortBy !== 'original-order') {
							a = getSorter( alpha, 'original-order' );
							b = getSorter( beta, 'original-order' );
						}
						return ( ( a > b ) ? 1 : ( a < b ) ? -1 : 0 ) * sortDir;
					};

			this.$filteredAtoms.sort( sortFn );
		},

		_getSorter : function( elem, sortBy ) {
			return $.data( elem, 'isotope-sort-data' )[ sortBy ];
		},

		// ====================== Layout Helpers ======================

		_translate : function( x, y ) {
			return { translate : [ x, y ] };
		},

		_positionAbs : function( x, y ) {
			return { left: x, top: y };
		},

		_pushPosition : function( $elem, x, y ) {
			x = Math.round( x + this.offset.left );
			y = Math.round( y + this.offset.top );
			var position = this.getPositionStyles( x, y );
			this.styleQueue.push({ $el: $elem, style: position });
			if ( this.options.itemPositionDataEnabled ) {
				$elem.data('isotope-item-position', {x: x, y: y} );
			}
		},


		// ====================== General Layout ======================

		// used on collection of atoms (should be filtered, and sorted before )
		// accepts atoms-to-be-laid-out to start with
		layout : function( $elems, callback ) {

			var layoutMode = this.options.layoutMode;

			// layout logic
			this[ '_' +  layoutMode + 'Layout' ]( $elems );

			// set the size of the container
			if ( this.options.resizesContainer ) {
				var containerStyle = this[ '_' +  layoutMode + 'GetContainerSize' ]();
				this.styleQueue.push({ $el: this.element, style: containerStyle });
			}

			this._processStyleQueue( $elems, callback );

			this.isLaidOut = true;
		},

		_processStyleQueue : function( $elems, callback ) {
			// are we animating the layout arrangement?
			// use plugin-ish syntax for css or animate
			var styleFn = !this.isLaidOut ? 'css' : (
						this.isUsingJQueryAnimation ? 'animate' : 'css'
					),
					animOpts = this.options.animationOptions,
					onLayout = this.options.onLayout,
					objStyleFn, processor,
					triggerCallbackNow, callbackFn;

			// default styleQueue processor, may be overwritten down below
			processor = function( i, obj ) {
				obj.$el[ styleFn ]( obj.style, animOpts );
			};

			if ( this._isInserting && this.isUsingJQueryAnimation ) {
				// if using styleQueue to insert items
				processor = function( i, obj ) {
					// only animate if it not being inserted
					objStyleFn = obj.$el.hasClass('no-transition') ? 'css' : styleFn;
					obj.$el[ objStyleFn ]( obj.style, animOpts );
				};

			} else if ( callback || onLayout || animOpts.complete ) {
				// has callback
				var isCallbackTriggered = false,
						// array of possible callbacks to trigger
						callbacks = [ callback, onLayout, animOpts.complete ],
						instance = this;
				triggerCallbackNow = true;
				// trigger callback only once
				callbackFn = function() {
					if ( isCallbackTriggered ) {
						return;
					}
					var hollaback;
					for (var i=0, len = callbacks.length; i < len; i++) {
						hollaback = callbacks[i];
						if ( typeof hollaback === 'function' ) {
							hollaback.call( instance.element, $elems, instance );
						}
					}
					isCallbackTriggered = true;
				};

				if ( this.isUsingJQueryAnimation && styleFn === 'animate' ) {
					// add callback to animation options
					animOpts.complete = callbackFn;
					triggerCallbackNow = false;

				} else if ( Modernizr.csstransitions ) {
					// detect if first item has transition
					var i = 0,
							firstItem = this.styleQueue[0],
							testElem = firstItem && firstItem.$el,
							styleObj;
					// get first non-empty jQ object
					while ( !testElem || !testElem.length ) {
						styleObj = this.styleQueue[ i++ ];
						// HACK: sometimes styleQueue[i] is undefined
						if ( !styleObj ) {
							return;
						}
						testElem = styleObj.$el;
					}
					// get transition duration of the first element in that object
					// yeah, this is inexact
					var duration = parseFloat( getComputedStyle( testElem[0] )[ transitionDurProp ] );
					if ( duration > 0 ) {
						processor = function( i, obj ) {
							obj.$el[ styleFn ]( obj.style, animOpts )
								// trigger callback at transition end
								.one( transitionEndEvent, callbackFn );
						};
						triggerCallbackNow = false;
					}
				}
			}

			// process styleQueue
			$.each( this.styleQueue, processor );

			if ( triggerCallbackNow ) {
				callbackFn();
			}

			// clear out queue for next time
			this.styleQueue = [];
		},


		resize : function() {
			if ( this[ '_' + this.options.layoutMode + 'ResizeChanged' ]() ) {
				this.reLayout();
			}
		},


		reLayout : function( callback ) {

			this[ '_' +  this.options.layoutMode + 'Reset' ]();
			this.layout( this.$filteredAtoms, callback );

		},

		// ====================== Convenience methods ======================

		// ====================== Adding items ======================

		// adds a jQuery object of items to a isotope container
		addItems : function( $content, callback ) {
			var $newAtoms = this._getAtoms( $content );
			// add new atoms to atoms pools
			this.$allAtoms = this.$allAtoms.add( $newAtoms );

			if ( callback ) {
				callback( $newAtoms );
			}
		},

		// convienence method for adding elements properly to any layout
		// positions items, hides them, then animates them back in <--- very sezzy
		insert : function( $content, callback ) {
			// position items
			this.element.append( $content );

			var instance = this;
			this.addItems( $content, function( $newAtoms ) {
				var $newFilteredAtoms = instance._filter( $newAtoms );
				instance._addHideAppended( $newFilteredAtoms );
				instance._sort();
				instance.reLayout();
				instance._revealAppended( $newFilteredAtoms, callback );
			});

		},

		// convienence method for working with Infinite Scroll
		appended : function( $content, callback ) {
			var instance = this;
			this.addItems( $content, function( $newAtoms ) {
				instance._addHideAppended( $newAtoms );
				instance.layout( $newAtoms );
				instance._revealAppended( $newAtoms, callback );
			});
		},

		// adds new atoms, then hides them before positioning
		_addHideAppended : function( $newAtoms ) {
			this.$filteredAtoms = this.$filteredAtoms.add( $newAtoms );
			$newAtoms.addClass('no-transition');

			this._isInserting = true;

			// apply hidden styles
			this.styleQueue.push({ $el: $newAtoms, style: this.options.hiddenStyle });
		},

		// sets visible style on new atoms
		_revealAppended : function( $newAtoms, callback ) {
			var instance = this;
			// apply visible style after a sec
			setTimeout( function() {
				// enable animation
				$newAtoms.removeClass('no-transition');
				// reveal newly inserted filtered elements
				instance.styleQueue.push({ $el: $newAtoms, style: instance.options.visibleStyle });
				instance._isInserting = false;
				instance._processStyleQueue( $newAtoms, callback );
			}, 10 );
		},

		// gathers all atoms
		reloadItems : function() {
			this.$allAtoms = this._getAtoms( this.element.children() );
		},

		// removes elements from Isotope widget
		remove: function( $content, callback ) {
			// remove elements from Isotope instance in callback
			var instance = this;
			// remove() as a callback, for after transition / animation
			var removeContent = function() {
				instance.$allAtoms = instance.$allAtoms.not( $content );
				$content.remove();
				if ( callback ) {
					callback.call( instance.element );
				}
			};

			if ( $content.filter( ':not(.' + this.options.hiddenClass + ')' ).length ) {
				// if any non-hidden content needs to be removed
				this.styleQueue.push({ $el: $content, style: this.options.hiddenStyle });
				this.$filteredAtoms = this.$filteredAtoms.not( $content );
				this._sort();
				this.reLayout( removeContent );
			} else {
				// remove it now
				removeContent();
			}

		},

		shuffle : function( callback ) {
			this.updateSortData( this.$allAtoms );
			this.options.sortBy = 'random';
			this._sort();
			this.reLayout( callback );
		},

		// destroys widget, returns elements and container back (close) to original style
		destroy : function() {

			var usingTransforms = this.usingTransforms;
			var options = this.options;

			this.$allAtoms
				.removeClass( options.hiddenClass + ' ' + options.itemClass )
				.each(function(){
					var style = this.style;
					style.position = '';
					style.top = '';
					style.left = '';
					style.opacity = '';
					if ( usingTransforms ) {
						style[ transformProp ] = '';
					}
				});

			// re-apply saved container styles
			var elemStyle = this.element[0].style;
			for ( var prop in this.originalStyle ) {
				elemStyle[ prop ] = this.originalStyle[ prop ];
			}

			this.element
				.unbind('.isotope')
				.undelegate( '.' + options.hiddenClass, 'click' )
				.removeClass( options.containerClass )
				.removeData('isotope');

			$window.unbind('.isotope');

		},


		// ====================== LAYOUTS ======================

		// calculates number of rows or columns
		// requires columnWidth or rowHeight to be set on namespaced object
		// i.e. this.masonry.columnWidth = 200
		_getSegments : function( isRows ) {
			var namespace = this.options.layoutMode,
					measure  = isRows ? 'rowHeight' : 'columnWidth',
					size     = isRows ? 'height' : 'width',
					segmentsName = isRows ? 'rows' : 'cols',
					containerSize = this.element[ size ](),
					segments,
										// i.e. options.masonry && options.masonry.columnWidth
					segmentSize = this.options[ namespace ] && this.options[ namespace ][ measure ] ||
										// or use the size of the first item, i.e. outerWidth
										this.$filteredAtoms[ 'outer' + capitalize(size) ](true) ||
										// if there's no items, use size of container
										containerSize;

			segments = Math.floor( containerSize / segmentSize );
			segments = Math.max( segments, 1 );

			// i.e. this.masonry.cols = ....
			this[ namespace ][ segmentsName ] = segments;
			// i.e. this.masonry.columnWidth = ...
			this[ namespace ][ measure ] = segmentSize;

		},

		_checkIfSegmentsChanged : function( isRows ) {
			var namespace = this.options.layoutMode,
					segmentsName = isRows ? 'rows' : 'cols',
					prevSegments = this[ namespace ][ segmentsName ];
			// update cols/rows
			this._getSegments( isRows );
			// return if updated cols/rows is not equal to previous
			return ( this[ namespace ][ segmentsName ] !== prevSegments );
		},

		// ====================== Masonry ======================

		_masonryReset : function() {
			// layout-specific props
			this.masonry = {};
			// FIXME shouldn't have to call this again
			this._getSegments();
			var i = this.masonry.cols;
			this.masonry.colYs = [];
			while (i--) {
				this.masonry.colYs.push( 0 );
			}
		},

		_masonryLayout : function( $elems ) {
			var instance = this,
					props = instance.masonry;
			$elems.each(function(){
				var $this  = $(this),
						//how many columns does this brick span
						colSpan = Math.ceil( $this.outerWidth(true) / props.columnWidth );
				colSpan = Math.min( colSpan, props.cols );

				if ( colSpan === 1 ) {
					// if brick spans only one column, just like singleMode
					instance._masonryPlaceBrick( $this, props.colYs );
				} else {
					// brick spans more than one column
					// how many different places could this brick fit horizontally
					var groupCount = props.cols + 1 - colSpan,
							groupY = [],
							groupColY,
							i;

					// for each group potential horizontal position
					for ( i=0; i < groupCount; i++ ) {
						// make an array of colY values for that one group
						groupColY = props.colYs.slice( i, i+colSpan );
						// and get the max value of the array
						groupY[i] = Math.max.apply( Math, groupColY );
					}

					instance._masonryPlaceBrick( $this, groupY );
				}
			});
		},

		// worker method that places brick in the columnSet
		//   with the the minY
		_masonryPlaceBrick : function( $brick, setY ) {
			// get the minimum Y value from the columns
			var minimumY = Math.min.apply( Math, setY ),
					shortCol = 0;

			// Find index of short column, the first from the left
			for (var i=0, len = setY.length; i < len; i++) {
				if ( setY[i] === minimumY ) {
					shortCol = i;
					break;
				}
			}

			// position the brick
			var x = this.masonry.columnWidth * shortCol,
					y = minimumY;
			this._pushPosition( $brick, x, y );

			// apply setHeight to necessary columns
			var setHeight = minimumY + $brick.outerHeight(true),
					setSpan = this.masonry.cols + 1 - len;
			for ( i=0; i < setSpan; i++ ) {
				this.masonry.colYs[ shortCol + i ] = setHeight;
			}

		},

		_masonryGetContainerSize : function() {
			var containerHeight = Math.max.apply( Math, this.masonry.colYs );
			return { height: containerHeight };
		},

		_masonryResizeChanged : function() {
			return this._checkIfSegmentsChanged();
		},

		// ====================== fitRows ======================

		_fitRowsReset : function() {
			this.fitRows = {
				x : 0,
				y : 0,
				height : 0
			};
		},

		_fitRowsLayout : function( $elems ) {
			var instance = this,
					containerWidth = this.element.width(),
					props = this.fitRows;

			$elems.each( function() {
				var $this = $(this),
						atomW = $this.outerWidth(true),
						atomH = $this.outerHeight(true);

				if ( props.x !== 0 && atomW + props.x > containerWidth ) {
					// if this element cannot fit in the current row
					props.x = 0;
					props.y = props.height;
				}

				// position the atom
				instance._pushPosition( $this, props.x, props.y );

				props.height = Math.max( props.y + atomH, props.height );
				props.x += atomW;

			});
		},

		_fitRowsGetContainerSize : function () {
			return { height : this.fitRows.height };
		},

		_fitRowsResizeChanged : function() {
			return true;
		},


		// ====================== cellsByRow ======================

		_cellsByRowReset : function() {
			this.cellsByRow = {
				index : 0
			};
			// get this.cellsByRow.columnWidth
			this._getSegments();
			// get this.cellsByRow.rowHeight
			this._getSegments(true);
		},

		_cellsByRowLayout : function( $elems ) {
			var instance = this,
					props = this.cellsByRow;
			$elems.each( function(){
				var $this = $(this),
						col = props.index % props.cols,
						row = Math.floor( props.index / props.cols ),
						x = ( col + 0.5 ) * props.columnWidth - $this.outerWidth(true) / 2,
						y = ( row + 0.5 ) * props.rowHeight - $this.outerHeight(true) / 2;
				instance._pushPosition( $this, x, y );
				props.index ++;
			});
		},

		_cellsByRowGetContainerSize : function() {
			return { height : Math.ceil( this.$filteredAtoms.length / this.cellsByRow.cols ) * this.cellsByRow.rowHeight + this.offset.top };
		},

		_cellsByRowResizeChanged : function() {
			return this._checkIfSegmentsChanged();
		},


		// ====================== straightDown ======================

		_straightDownReset : function() {
			this.straightDown = {
				y : 0
			};
		},

		_straightDownLayout : function( $elems ) {
			var instance = this;
			$elems.each( function( i ){
				var $this = $(this);
				instance._pushPosition( $this, 0, instance.straightDown.y );
				instance.straightDown.y += $this.outerHeight(true);
			});
		},

		_straightDownGetContainerSize : function() {
			return { height : this.straightDown.y };
		},

		_straightDownResizeChanged : function() {
			return true;
		},


		// ====================== masonryHorizontal ======================

		_masonryHorizontalReset : function() {
			// layout-specific props
			this.masonryHorizontal = {};
			// FIXME shouldn't have to call this again
			this._getSegments( true );
			var i = this.masonryHorizontal.rows;
			this.masonryHorizontal.rowXs = [];
			while (i--) {
				this.masonryHorizontal.rowXs.push( 0 );
			}
		},

		_masonryHorizontalLayout : function( $elems ) {
			var instance = this,
					props = instance.masonryHorizontal;
			$elems.each(function(){
				var $this  = $(this),
						//how many rows does this brick span
						rowSpan = Math.ceil( $this.outerHeight(true) / props.rowHeight );
				rowSpan = Math.min( rowSpan, props.rows );

				if ( rowSpan === 1 ) {
					// if brick spans only one column, just like singleMode
					instance._masonryHorizontalPlaceBrick( $this, props.rowXs );
				} else {
					// brick spans more than one row
					// how many different places could this brick fit horizontally
					var groupCount = props.rows + 1 - rowSpan,
							groupX = [],
							groupRowX, i;

					// for each group potential horizontal position
					for ( i=0; i < groupCount; i++ ) {
						// make an array of colY values for that one group
						groupRowX = props.rowXs.slice( i, i+rowSpan );
						// and get the max value of the array
						groupX[i] = Math.max.apply( Math, groupRowX );
					}

					instance._masonryHorizontalPlaceBrick( $this, groupX );
				}
			});
		},

		_masonryHorizontalPlaceBrick : function( $brick, setX ) {
			// get the minimum Y value from the columns
			var minimumX  = Math.min.apply( Math, setX ),
					smallRow  = 0;
			// Find index of smallest row, the first from the top
			for (var i=0, len = setX.length; i < len; i++) {
				if ( setX[i] === minimumX ) {
					smallRow = i;
					break;
				}
			}

			// position the brick
			var x = minimumX,
					y = this.masonryHorizontal.rowHeight * smallRow;
			this._pushPosition( $brick, x, y );

			// apply setHeight to necessary columns
			var setWidth = minimumX + $brick.outerWidth(true),
					setSpan = this.masonryHorizontal.rows + 1 - len;
			for ( i=0; i < setSpan; i++ ) {
				this.masonryHorizontal.rowXs[ smallRow + i ] = setWidth;
			}
		},

		_masonryHorizontalGetContainerSize : function() {
			var containerWidth = Math.max.apply( Math, this.masonryHorizontal.rowXs );
			return { width: containerWidth };
		},

		_masonryHorizontalResizeChanged : function() {
			return this._checkIfSegmentsChanged(true);
		},


		// ====================== fitColumns ======================

		_fitColumnsReset : function() {
			this.fitColumns = {
				x : 0,
				y : 0,
				width : 0
			};
		},

		_fitColumnsLayout : function( $elems ) {
			var instance = this,
					containerHeight = this.element.height(),
					props = this.fitColumns;
			$elems.each( function() {
				var $this = $(this),
						atomW = $this.outerWidth(true),
						atomH = $this.outerHeight(true);

				if ( props.y !== 0 && atomH + props.y > containerHeight ) {
					// if this element cannot fit in the current column
					props.x = props.width;
					props.y = 0;
				}

				// position the atom
				instance._pushPosition( $this, props.x, props.y );

				props.width = Math.max( props.x + atomW, props.width );
				props.y += atomH;

			});
		},

		_fitColumnsGetContainerSize : function () {
			return { width : this.fitColumns.width };
		},

		_fitColumnsResizeChanged : function() {
			return true;
		},



		// ====================== cellsByColumn ======================

		_cellsByColumnReset : function() {
			this.cellsByColumn = {
				index : 0
			};
			// get this.cellsByColumn.columnWidth
			this._getSegments();
			// get this.cellsByColumn.rowHeight
			this._getSegments(true);
		},

		_cellsByColumnLayout : function( $elems ) {
			var instance = this,
					props = this.cellsByColumn;
			$elems.each( function(){
				var $this = $(this),
						col = Math.floor( props.index / props.rows ),
						row = props.index % props.rows,
						x = ( col + 0.5 ) * props.columnWidth - $this.outerWidth(true) / 2,
						y = ( row + 0.5 ) * props.rowHeight - $this.outerHeight(true) / 2;
				instance._pushPosition( $this, x, y );
				props.index ++;
			});
		},

		_cellsByColumnGetContainerSize : function() {
			return { width : Math.ceil( this.$filteredAtoms.length / this.cellsByColumn.rows ) * this.cellsByColumn.columnWidth };
		},

		_cellsByColumnResizeChanged : function() {
			return this._checkIfSegmentsChanged(true);
		},

		// ====================== straightAcross ======================

		_straightAcrossReset : function() {
			this.straightAcross = {
				x : 0
			};
		},

		_straightAcrossLayout : function( $elems ) {
			var instance = this;
			$elems.each( function( i ){
				var $this = $(this);
				instance._pushPosition( $this, instance.straightAcross.x, 0 );
				instance.straightAcross.x += $this.outerWidth(true);
			});
		},

		_straightAcrossGetContainerSize : function() {
			return { width : this.straightAcross.x };
		},

		_straightAcrossResizeChanged : function() {
			return true;
		}

	};


	// ======================= imagesLoaded Plugin ===============================
	/*!
	 * jQuery imagesLoaded plugin v1.1.0
	 * http://github.com/desandro/imagesloaded
	 *
	 * MIT License. by Paul Irish et al.
	 */


	// $('#my-container').imagesLoaded(myFunction)
	// or
	// $('img').imagesLoaded(myFunction)

	// execute a callback when all images have loaded.
	// needed because .load() doesn't work on cached images

	// callback function gets image collection as argument
	//  `this` is the container

	$.fn.imagesLoaded = function( callback ) {
		var $this = this,
				$images = $this.find('img').add( $this.filter('img') ),
				len = $images.length,
				blank = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==',
				loaded = [];

		function triggerCallback() {
			callback.call( $this, $images );
		}

		function imgLoaded( event ) {
			var img = event.target;
			if ( img.src !== blank && $.inArray( img, loaded ) === -1 ){
				loaded.push( img );
				if ( --len <= 0 ){
					setTimeout( triggerCallback );
					$images.unbind( '.imagesLoaded', imgLoaded );
				}
			}
		}

		// if no images, trigger immediately
		if ( !len ) {
			triggerCallback();
		}

		$images.bind( 'load.imagesLoaded error.imagesLoaded',  imgLoaded ).each( function() {
			// cached images don't fire load sometimes, so we reset src.
			var src = this.src;
			// webkit hack from http://groups.google.com/group/jquery-dev/browse_thread/thread/eee6ab7b2da50e1f
			// data uri bypasses webkit log warning (thx doug jones)
			this.src = blank;
			this.src = src;
		});

		return $this;
	};


	// helper function for logging errors
	// $.error breaks jQuery chaining
	var logError = function( message ) {
		if ( window.console ) {
			window.console.error( message );
		}
	};

	// =======================  Plugin bridge  ===============================
	// leverages data method to either create or return $.Isotope constructor
	// A bit from jQuery UI
	//   https://github.com/jquery/jquery-ui/blob/master/ui/jquery.ui.widget.js
	// A bit from jcarousel
	//   https://github.com/jsor/jcarousel/blob/master/lib/jquery.jcarousel.js

	$.fn.isotope = function( options, callback ) {
		if ( typeof options === 'string' ) {
			// call method
			var args = Array.prototype.slice.call( arguments, 1 );

			this.each(function(){
				var instance = $.data( this, 'isotope' );
				if ( !instance ) {
					logError( "cannot call methods on isotope prior to initialization; " +
							"attempted to call method '" + options + "'" );
					return;
				}
				if ( !$.isFunction( instance[options] ) || options.charAt(0) === "_" ) {
					logError( "no such method '" + options + "' for isotope instance" );
					return;
				}
				// apply method
				instance[ options ].apply( instance, args );
			});
		} else {
			this.each(function() {
				var instance = $.data( this, 'isotope' );
				if ( instance ) {
					// apply options & init
					instance.option( options );
					instance._init( callback );
				} else {
					// initialize new instance
					$.data( this, 'isotope', new $.Isotope( options, this, callback ) );
				}
			});
		}
		// return jQuery object
		// so plugin methods do not have to
		return this;
	};

})( window, jQuery );

// lib/handlebars/base.js

/*jshint eqnull:true*/
this.Handlebars = {};

(function(Handlebars) {

Handlebars.VERSION = "1.0.rc.1";

Handlebars.helpers  = {};
Handlebars.partials = {};

Handlebars.registerHelper = function(name, fn, inverse) {
	if(inverse) { fn.not = inverse; }
	this.helpers[name] = fn;
};

Handlebars.registerPartial = function(name, str) {
	this.partials[name] = str;
};

Handlebars.registerHelper('helperMissing', function(arg) {
	if(arguments.length === 2) {
		return undefined;
	} else {
		throw new Error("Could not find property '" + arg + "'");
	}
});

var toString = Object.prototype.toString, functionType = "[object Function]";

Handlebars.registerHelper('blockHelperMissing', function(context, options) {
	var inverse = options.inverse || function() {}, fn = options.fn;


	var ret = "";
	var type = toString.call(context);

	if(type === functionType) { context = context.call(this); }

	if(context === true) {
		return fn(this);
	} else if(context === false || context == null) {
		return inverse(this);
	} else if(type === "[object Array]") {
		if(context.length > 0) {
			return Handlebars.helpers.each(context, options);
		} else {
			return inverse(this);
		}
	} else {
		return fn(context);
	}
});

Handlebars.K = function() {};

Handlebars.createFrame = Object.create || function(object) {
	Handlebars.K.prototype = object;
	var obj = new Handlebars.K();
	Handlebars.K.prototype = null;
	return obj;
};

Handlebars.registerHelper('each', function(context, options) {
	var fn = options.fn, inverse = options.inverse;
	var ret = "", data;

	if (options.data) {
		data = Handlebars.createFrame(options.data);
	}

	if(context && context.length > 0) {
		for(var i=0, j=context.length; i<j; i++) {
			if (data) { data.index = i; }
			ret = ret + fn(context[i], { data: data });
		}
	} else {
		ret = inverse(this);
	}
	return ret;
});

Handlebars.registerHelper('if', function(context, options) {
	var type = toString.call(context);
	if(type === functionType) { context = context.call(this); }

	if(!context || Handlebars.Utils.isEmpty(context)) {
		return options.inverse(this);
	} else {
		return options.fn(this);
	}
});

Handlebars.registerHelper('unless', function(context, options) {
	var fn = options.fn, inverse = options.inverse;
	options.fn = inverse;
	options.inverse = fn;

	return Handlebars.helpers['if'].call(this, context, options);
});

Handlebars.registerHelper('with', function(context, options) {
	return options.fn(context);
});

Handlebars.registerHelper('log', function(context) {
	Handlebars.log(context);
});

}(this.Handlebars));
;
// lib/handlebars/compiler/parser.js
/* Jison generated parser */
var handlebars = (function(){
var parser = {trace: function trace() { },
yy: {},
symbols_: {"error":2,"root":3,"program":4,"EOF":5,"statements":6,"simpleInverse":7,"statement":8,"openInverse":9,"closeBlock":10,"openBlock":11,"mustache":12,"partial":13,"CONTENT":14,"COMMENT":15,"OPEN_BLOCK":16,"inMustache":17,"CLOSE":18,"OPEN_INVERSE":19,"OPEN_ENDBLOCK":20,"path":21,"OPEN":22,"OPEN_UNESCAPED":23,"OPEN_PARTIAL":24,"params":25,"hash":26,"DATA":27,"param":28,"STRING":29,"INTEGER":30,"BOOLEAN":31,"hashSegments":32,"hashSegment":33,"ID":34,"EQUALS":35,"pathSegments":36,"SEP":37,"$accept":0,"$end":1},
terminals_: {2:"error",5:"EOF",14:"CONTENT",15:"COMMENT",16:"OPEN_BLOCK",18:"CLOSE",19:"OPEN_INVERSE",20:"OPEN_ENDBLOCK",22:"OPEN",23:"OPEN_UNESCAPED",24:"OPEN_PARTIAL",27:"DATA",29:"STRING",30:"INTEGER",31:"BOOLEAN",34:"ID",35:"EQUALS",37:"SEP"},
productions_: [0,[3,2],[4,3],[4,1],[4,0],[6,1],[6,2],[8,3],[8,3],[8,1],[8,1],[8,1],[8,1],[11,3],[9,3],[10,3],[12,3],[12,3],[13,3],[13,4],[7,2],[17,3],[17,2],[17,2],[17,1],[17,1],[25,2],[25,1],[28,1],[28,1],[28,1],[28,1],[28,1],[26,1],[32,2],[32,1],[33,3],[33,3],[33,3],[33,3],[33,3],[21,1],[36,3],[36,1]],
performAction: function anonymous(yytext,yyleng,yylineno,yy,yystate,$$,_$) {

var $0 = $$.length - 1;
switch (yystate) {
case 1: return $$[$0-1];
break;
case 2: this.$ = new yy.ProgramNode($$[$0-2], $$[$0]);
break;
case 3: this.$ = new yy.ProgramNode($$[$0]);
break;
case 4: this.$ = new yy.ProgramNode([]);
break;
case 5: this.$ = [$$[$0]];
break;
case 6: $$[$0-1].push($$[$0]); this.$ = $$[$0-1];
break;
case 7: this.$ = new yy.BlockNode($$[$0-2], $$[$0-1].inverse, $$[$0-1], $$[$0]);
break;
case 8: this.$ = new yy.BlockNode($$[$0-2], $$[$0-1], $$[$0-1].inverse, $$[$0]);
break;
case 9: this.$ = $$[$0];
break;
case 10: this.$ = $$[$0];
break;
case 11: this.$ = new yy.ContentNode($$[$0]);
break;
case 12: this.$ = new yy.CommentNode($$[$0]);
break;
case 13: this.$ = new yy.MustacheNode($$[$0-1][0], $$[$0-1][1]);
break;
case 14: this.$ = new yy.MustacheNode($$[$0-1][0], $$[$0-1][1]);
break;
case 15: this.$ = $$[$0-1];
break;
case 16: this.$ = new yy.MustacheNode($$[$0-1][0], $$[$0-1][1]);
break;
case 17: this.$ = new yy.MustacheNode($$[$0-1][0], $$[$0-1][1], true);
break;
case 18: this.$ = new yy.PartialNode($$[$0-1]);
break;
case 19: this.$ = new yy.PartialNode($$[$0-2], $$[$0-1]);
break;
case 20:
break;
case 21: this.$ = [[$$[$0-2]].concat($$[$0-1]), $$[$0]];
break;
case 22: this.$ = [[$$[$0-1]].concat($$[$0]), null];
break;
case 23: this.$ = [[$$[$0-1]], $$[$0]];
break;
case 24: this.$ = [[$$[$0]], null];
break;
case 25: this.$ = [[new yy.DataNode($$[$0])], null];
break;
case 26: $$[$0-1].push($$[$0]); this.$ = $$[$0-1];
break;
case 27: this.$ = [$$[$0]];
break;
case 28: this.$ = $$[$0];
break;
case 29: this.$ = new yy.StringNode($$[$0]);
break;
case 30: this.$ = new yy.IntegerNode($$[$0]);
break;
case 31: this.$ = new yy.BooleanNode($$[$0]);
break;
case 32: this.$ = new yy.DataNode($$[$0]);
break;
case 33: this.$ = new yy.HashNode($$[$0]);
break;
case 34: $$[$0-1].push($$[$0]); this.$ = $$[$0-1];
break;
case 35: this.$ = [$$[$0]];
break;
case 36: this.$ = [$$[$0-2], $$[$0]];
break;
case 37: this.$ = [$$[$0-2], new yy.StringNode($$[$0])];
break;
case 38: this.$ = [$$[$0-2], new yy.IntegerNode($$[$0])];
break;
case 39: this.$ = [$$[$0-2], new yy.BooleanNode($$[$0])];
break;
case 40: this.$ = [$$[$0-2], new yy.DataNode($$[$0])];
break;
case 41: this.$ = new yy.IdNode($$[$0]);
break;
case 42: $$[$0-2].push($$[$0]); this.$ = $$[$0-2];
break;
case 43: this.$ = [$$[$0]];
break;
}
},
table: [{3:1,4:2,5:[2,4],6:3,8:4,9:5,11:6,12:7,13:8,14:[1,9],15:[1,10],16:[1,12],19:[1,11],22:[1,13],23:[1,14],24:[1,15]},{1:[3]},{5:[1,16]},{5:[2,3],7:17,8:18,9:5,11:6,12:7,13:8,14:[1,9],15:[1,10],16:[1,12],19:[1,19],20:[2,3],22:[1,13],23:[1,14],24:[1,15]},{5:[2,5],14:[2,5],15:[2,5],16:[2,5],19:[2,5],20:[2,5],22:[2,5],23:[2,5],24:[2,5]},{4:20,6:3,8:4,9:5,11:6,12:7,13:8,14:[1,9],15:[1,10],16:[1,12],19:[1,11],20:[2,4],22:[1,13],23:[1,14],24:[1,15]},{4:21,6:3,8:4,9:5,11:6,12:7,13:8,14:[1,9],15:[1,10],16:[1,12],19:[1,11],20:[2,4],22:[1,13],23:[1,14],24:[1,15]},{5:[2,9],14:[2,9],15:[2,9],16:[2,9],19:[2,9],20:[2,9],22:[2,9],23:[2,9],24:[2,9]},{5:[2,10],14:[2,10],15:[2,10],16:[2,10],19:[2,10],20:[2,10],22:[2,10],23:[2,10],24:[2,10]},{5:[2,11],14:[2,11],15:[2,11],16:[2,11],19:[2,11],20:[2,11],22:[2,11],23:[2,11],24:[2,11]},{5:[2,12],14:[2,12],15:[2,12],16:[2,12],19:[2,12],20:[2,12],22:[2,12],23:[2,12],24:[2,12]},{17:22,21:23,27:[1,24],34:[1,26],36:25},{17:27,21:23,27:[1,24],34:[1,26],36:25},{17:28,21:23,27:[1,24],34:[1,26],36:25},{17:29,21:23,27:[1,24],34:[1,26],36:25},{21:30,34:[1,26],36:25},{1:[2,1]},{6:31,8:4,9:5,11:6,12:7,13:8,14:[1,9],15:[1,10],16:[1,12],19:[1,11],22:[1,13],23:[1,14],24:[1,15]},{5:[2,6],14:[2,6],15:[2,6],16:[2,6],19:[2,6],20:[2,6],22:[2,6],23:[2,6],24:[2,6]},{17:22,18:[1,32],21:23,27:[1,24],34:[1,26],36:25},{10:33,20:[1,34]},{10:35,20:[1,34]},{18:[1,36]},{18:[2,24],21:41,25:37,26:38,27:[1,45],28:39,29:[1,42],30:[1,43],31:[1,44],32:40,33:46,34:[1,47],36:25},{18:[2,25]},{18:[2,41],27:[2,41],29:[2,41],30:[2,41],31:[2,41],34:[2,41],37:[1,48]},{18:[2,43],27:[2,43],29:[2,43],30:[2,43],31:[2,43],34:[2,43],37:[2,43]},{18:[1,49]},{18:[1,50]},{18:[1,51]},{18:[1,52],21:53,34:[1,26],36:25},{5:[2,2],8:18,9:5,11:6,12:7,13:8,14:[1,9],15:[1,10],16:[1,12],19:[1,11],20:[2,2],22:[1,13],23:[1,14],24:[1,15]},{14:[2,20],15:[2,20],16:[2,20],19:[2,20],22:[2,20],23:[2,20],24:[2,20]},{5:[2,7],14:[2,7],15:[2,7],16:[2,7],19:[2,7],20:[2,7],22:[2,7],23:[2,7],24:[2,7]},{21:54,34:[1,26],36:25},{5:[2,8],14:[2,8],15:[2,8],16:[2,8],19:[2,8],20:[2,8],22:[2,8],23:[2,8],24:[2,8]},{14:[2,14],15:[2,14],16:[2,14],19:[2,14],20:[2,14],22:[2,14],23:[2,14],24:[2,14]},{18:[2,22],21:41,26:55,27:[1,45],28:56,29:[1,42],30:[1,43],31:[1,44],32:40,33:46,34:[1,47],36:25},{18:[2,23]},{18:[2,27],27:[2,27],29:[2,27],30:[2,27],31:[2,27],34:[2,27]},{18:[2,33],33:57,34:[1,58]},{18:[2,28],27:[2,28],29:[2,28],30:[2,28],31:[2,28],34:[2,28]},{18:[2,29],27:[2,29],29:[2,29],30:[2,29],31:[2,29],34:[2,29]},{18:[2,30],27:[2,30],29:[2,30],30:[2,30],31:[2,30],34:[2,30]},{18:[2,31],27:[2,31],29:[2,31],30:[2,31],31:[2,31],34:[2,31]},{18:[2,32],27:[2,32],29:[2,32],30:[2,32],31:[2,32],34:[2,32]},{18:[2,35],34:[2,35]},{18:[2,43],27:[2,43],29:[2,43],30:[2,43],31:[2,43],34:[2,43],35:[1,59],37:[2,43]},{34:[1,60]},{14:[2,13],15:[2,13],16:[2,13],19:[2,13],20:[2,13],22:[2,13],23:[2,13],24:[2,13]},{5:[2,16],14:[2,16],15:[2,16],16:[2,16],19:[2,16],20:[2,16],22:[2,16],23:[2,16],24:[2,16]},{5:[2,17],14:[2,17],15:[2,17],16:[2,17],19:[2,17],20:[2,17],22:[2,17],23:[2,17],24:[2,17]},{5:[2,18],14:[2,18],15:[2,18],16:[2,18],19:[2,18],20:[2,18],22:[2,18],23:[2,18],24:[2,18]},{18:[1,61]},{18:[1,62]},{18:[2,21]},{18:[2,26],27:[2,26],29:[2,26],30:[2,26],31:[2,26],34:[2,26]},{18:[2,34],34:[2,34]},{35:[1,59]},{21:63,27:[1,67],29:[1,64],30:[1,65],31:[1,66],34:[1,26],36:25},{18:[2,42],27:[2,42],29:[2,42],30:[2,42],31:[2,42],34:[2,42],37:[2,42]},{5:[2,19],14:[2,19],15:[2,19],16:[2,19],19:[2,19],20:[2,19],22:[2,19],23:[2,19],24:[2,19]},{5:[2,15],14:[2,15],15:[2,15],16:[2,15],19:[2,15],20:[2,15],22:[2,15],23:[2,15],24:[2,15]},{18:[2,36],34:[2,36]},{18:[2,37],34:[2,37]},{18:[2,38],34:[2,38]},{18:[2,39],34:[2,39]},{18:[2,40],34:[2,40]}],
defaultActions: {16:[2,1],24:[2,25],38:[2,23],55:[2,21]},
parseError: function parseError(str, hash) {
		throw new Error(str);
},
parse: function parse(input) {
		var self = this, stack = [0], vstack = [null], lstack = [], table = this.table, yytext = "", yylineno = 0, yyleng = 0, recovering = 0, TERROR = 2, EOF = 1;
		this.lexer.setInput(input);
		this.lexer.yy = this.yy;
		this.yy.lexer = this.lexer;
		this.yy.parser = this;
		if (typeof this.lexer.yylloc == "undefined")
				this.lexer.yylloc = {};
		var yyloc = this.lexer.yylloc;
		lstack.push(yyloc);
		var ranges = this.lexer.options && this.lexer.options.ranges;
		if (typeof this.yy.parseError === "function")
				this.parseError = this.yy.parseError;
		function popStack(n) {
				stack.length = stack.length - 2 * n;
				vstack.length = vstack.length - n;
				lstack.length = lstack.length - n;
		}
		function lex() {
				var token;
				token = self.lexer.lex() || 1;
				if (typeof token !== "number") {
						token = self.symbols_[token] || token;
				}
				return token;
		}
		var symbol, preErrorSymbol, state, action, a, r, yyval = {}, p, len, newState, expected;
		while (true) {
				state = stack[stack.length - 1];
				if (this.defaultActions[state]) {
						action = this.defaultActions[state];
				} else {
						if (symbol === null || typeof symbol == "undefined") {
								symbol = lex();
						}
						action = table[state] && table[state][symbol];
				}
				if (typeof action === "undefined" || !action.length || !action[0]) {
						var errStr = "";
						if (!recovering) {
								expected = [];
								for (p in table[state])
										if (this.terminals_[p] && p > 2) {
												expected.push("'" + this.terminals_[p] + "'");
										}
								if (this.lexer.showPosition) {
										errStr = "Parse error on line " + (yylineno + 1) + ":\n" + this.lexer.showPosition() + "\nExpecting " + expected.join(", ") + ", got '" + (this.terminals_[symbol] || symbol) + "'";
								} else {
										errStr = "Parse error on line " + (yylineno + 1) + ": Unexpected " + (symbol == 1?"end of input":"'" + (this.terminals_[symbol] || symbol) + "'");
								}
								this.parseError(errStr, {text: this.lexer.match, token: this.terminals_[symbol] || symbol, line: this.lexer.yylineno, loc: yyloc, expected: expected});
						}
				}
				if (action[0] instanceof Array && action.length > 1) {
						throw new Error("Parse Error: multiple actions possible at state: " + state + ", token: " + symbol);
				}
				switch (action[0]) {
				case 1:
						stack.push(symbol);
						vstack.push(this.lexer.yytext);
						lstack.push(this.lexer.yylloc);
						stack.push(action[1]);
						symbol = null;
						if (!preErrorSymbol) {
								yyleng = this.lexer.yyleng;
								yytext = this.lexer.yytext;
								yylineno = this.lexer.yylineno;
								yyloc = this.lexer.yylloc;
								if (recovering > 0)
										recovering--;
						} else {
								symbol = preErrorSymbol;
								preErrorSymbol = null;
						}
						break;
				case 2:
						len = this.productions_[action[1]][1];
						yyval.$ = vstack[vstack.length - len];
						yyval._$ = {first_line: lstack[lstack.length - (len || 1)].first_line, last_line: lstack[lstack.length - 1].last_line, first_column: lstack[lstack.length - (len || 1)].first_column, last_column: lstack[lstack.length - 1].last_column};
						if (ranges) {
								yyval._$.range = [lstack[lstack.length - (len || 1)].range[0], lstack[lstack.length - 1].range[1]];
						}
						r = this.performAction.call(yyval, yytext, yyleng, yylineno, this.yy, action[1], vstack, lstack);
						if (typeof r !== "undefined") {
								return r;
						}
						if (len) {
								stack = stack.slice(0, -1 * len * 2);
								vstack = vstack.slice(0, -1 * len);
								lstack = lstack.slice(0, -1 * len);
						}
						stack.push(this.productions_[action[1]][0]);
						vstack.push(yyval.$);
						lstack.push(yyval._$);
						newState = table[stack[stack.length - 2]][stack[stack.length - 1]];
						stack.push(newState);
						break;
				case 3:
						return true;
				}
		}
		return true;
}
};
/* Jison generated lexer */
var lexer = (function(){
var lexer = ({EOF:1,
parseError:function parseError(str, hash) {
				if (this.yy.parser) {
						this.yy.parser.parseError(str, hash);
				} else {
						throw new Error(str);
				}
		},
setInput:function (input) {
				this._input = input;
				this._more = this._less = this.done = false;
				this.yylineno = this.yyleng = 0;
				this.yytext = this.matched = this.match = '';
				this.conditionStack = ['INITIAL'];
				this.yylloc = {first_line:1,first_column:0,last_line:1,last_column:0};
				if (this.options.ranges) this.yylloc.range = [0,0];
				this.offset = 0;
				return this;
		},
input:function () {
				var ch = this._input[0];
				this.yytext += ch;
				this.yyleng++;
				this.offset++;
				this.match += ch;
				this.matched += ch;
				var lines = ch.match(/(?:\r\n?|\n).*/g);
				if (lines) {
						this.yylineno++;
						this.yylloc.last_line++;
				} else {
						this.yylloc.last_column++;
				}
				if (this.options.ranges) this.yylloc.range[1]++;

				this._input = this._input.slice(1);
				return ch;
		},
unput:function (ch) {
				var len = ch.length;
				var lines = ch.split(/(?:\r\n?|\n)/g);

				this._input = ch + this._input;
				this.yytext = this.yytext.substr(0, this.yytext.length-len-1);
				//this.yyleng -= len;
				this.offset -= len;
				var oldLines = this.match.split(/(?:\r\n?|\n)/g);
				this.match = this.match.substr(0, this.match.length-1);
				this.matched = this.matched.substr(0, this.matched.length-1);

				if (lines.length-1) this.yylineno -= lines.length-1;
				var r = this.yylloc.range;

				this.yylloc = {first_line: this.yylloc.first_line,
					last_line: this.yylineno+1,
					first_column: this.yylloc.first_column,
					last_column: lines ?
							(lines.length === oldLines.length ? this.yylloc.first_column : 0) + oldLines[oldLines.length - lines.length].length - lines[0].length:
							this.yylloc.first_column - len
					};

				if (this.options.ranges) {
						this.yylloc.range = [r[0], r[0] + this.yyleng - len];
				}
				return this;
		},
more:function () {
				this._more = true;
				return this;
		},
less:function (n) {
				this.unput(this.match.slice(n));
		},
pastInput:function () {
				var past = this.matched.substr(0, this.matched.length - this.match.length);
				return (past.length > 20 ? '...':'') + past.substr(-20).replace(/\n/g, "");
		},
upcomingInput:function () {
				var next = this.match;
				if (next.length < 20) {
						next += this._input.substr(0, 20-next.length);
				}
				return (next.substr(0,20)+(next.length > 20 ? '...':'')).replace(/\n/g, "");
		},
showPosition:function () {
				var pre = this.pastInput();
				var c = new Array(pre.length + 1).join("-");
				return pre + this.upcomingInput() + "\n" + c+"^";
		},
next:function () {
				if (this.done) {
						return this.EOF;
				}
				if (!this._input) this.done = true;

				var token,
						match,
						tempMatch,
						index,
						col,
						lines;
				if (!this._more) {
						this.yytext = '';
						this.match = '';
				}
				var rules = this._currentRules();
				for (var i=0;i < rules.length; i++) {
						tempMatch = this._input.match(this.rules[rules[i]]);
						if (tempMatch && (!match || tempMatch[0].length > match[0].length)) {
								match = tempMatch;
								index = i;
								if (!this.options.flex) break;
						}
				}
				if (match) {
						lines = match[0].match(/(?:\r\n?|\n).*/g);
						if (lines) this.yylineno += lines.length;
						this.yylloc = {first_line: this.yylloc.last_line,
													 last_line: this.yylineno+1,
													 first_column: this.yylloc.last_column,
													 last_column: lines ? lines[lines.length-1].length-lines[lines.length-1].match(/\r?\n?/)[0].length : this.yylloc.last_column + match[0].length};
						this.yytext += match[0];
						this.match += match[0];
						this.matches = match;
						this.yyleng = this.yytext.length;
						if (this.options.ranges) {
								this.yylloc.range = [this.offset, this.offset += this.yyleng];
						}
						this._more = false;
						this._input = this._input.slice(match[0].length);
						this.matched += match[0];
						token = this.performAction.call(this, this.yy, this, rules[index],this.conditionStack[this.conditionStack.length-1]);
						if (this.done && this._input) this.done = false;
						if (token) return token;
						else return;
				}
				if (this._input === "") {
						return this.EOF;
				} else {
						return this.parseError('Lexical error on line '+(this.yylineno+1)+'. Unrecognized text.\n'+this.showPosition(),
										{text: "", token: null, line: this.yylineno});
				}
		},
lex:function lex() {
				var r = this.next();
				if (typeof r !== 'undefined') {
						return r;
				} else {
						return this.lex();
				}
		},
begin:function begin(condition) {
				this.conditionStack.push(condition);
		},
popState:function popState() {
				return this.conditionStack.pop();
		},
_currentRules:function _currentRules() {
				return this.conditions[this.conditionStack[this.conditionStack.length-1]].rules;
		},
topState:function () {
				return this.conditionStack[this.conditionStack.length-2];
		},
pushState:function begin(condition) {
				this.begin(condition);
		}});
lexer.options = {};
lexer.performAction = function anonymous(yy,yy_,$avoiding_name_collisions,YY_START) {

var YYSTATE=YY_START
switch($avoiding_name_collisions) {
case 0:
																	 if(yy_.yytext.slice(-1) !== "\\") this.begin("mu");
																	 if(yy_.yytext.slice(-1) === "\\") yy_.yytext = yy_.yytext.substr(0,yy_.yyleng-1), this.begin("emu");
																	 if(yy_.yytext) return 14;

break;
case 1: return 14;
break;
case 2:
																	 if(yy_.yytext.slice(-1) !== "\\") this.popState();
																	 if(yy_.yytext.slice(-1) === "\\") yy_.yytext = yy_.yytext.substr(0,yy_.yyleng-1);
																	 return 14;

break;
case 3: return 24;
break;
case 4: return 16;
break;
case 5: return 20;
break;
case 6: return 19;
break;
case 7: return 19;
break;
case 8: return 23;
break;
case 9: return 23;
break;
case 10: yy_.yytext = yy_.yytext.substr(3,yy_.yyleng-5); this.popState(); return 15;
break;
case 11: return 22;
break;
case 12: return 35;
break;
case 13: return 34;
break;
case 14: return 34;
break;
case 15: return 37;
break;
case 16: /*ignore whitespace*/
break;
case 17: this.popState(); return 18;
break;
case 18: this.popState(); return 18;
break;
case 19: yy_.yytext = yy_.yytext.substr(1,yy_.yyleng-2).replace(/\\"/g,'"'); return 29;
break;
case 20: yy_.yytext = yy_.yytext.substr(1,yy_.yyleng-2).replace(/\\"/g,'"'); return 29;
break;
case 21: yy_.yytext = yy_.yytext.substr(1); return 27;
break;
case 22: return 31;
break;
case 23: return 31;
break;
case 24: return 30;
break;
case 25: return 34;
break;
case 26: yy_.yytext = yy_.yytext.substr(1, yy_.yyleng-2); return 34;
break;
case 27: return 'INVALID';
break;
case 28: return 5;
break;
}
};
lexer.rules = [/^(?:[^\x00]*?(?=(\{\{)))/,/^(?:[^\x00]+)/,/^(?:[^\x00]{2,}?(?=(\{\{|$)))/,/^(?:\{\{>)/,/^(?:\{\{#)/,/^(?:\{\{\/)/,/^(?:\{\{\^)/,/^(?:\{\{\s*else\b)/,/^(?:\{\{\{)/,/^(?:\{\{&)/,/^(?:\{\{![\s\S]*?\}\})/,/^(?:\{\{)/,/^(?:=)/,/^(?:\.(?=[} ]))/,/^(?:\.\.)/,/^(?:[\/.])/,/^(?:\s+)/,/^(?:\}\}\})/,/^(?:\}\})/,/^(?:"(\\["]|[^"])*")/,/^(?:'(\\[']|[^'])*')/,/^(?:@[a-zA-Z]+)/,/^(?:true(?=[}\s]))/,/^(?:false(?=[}\s]))/,/^(?:[0-9]+(?=[}\s]))/,/^(?:[a-zA-Z0-9_$-]+(?=[=}\s\/.]))/,/^(?:\[[^\]]*\])/,/^(?:.)/,/^(?:$)/];
lexer.conditions = {"mu":{"rules":[3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28],"inclusive":false},"emu":{"rules":[2],"inclusive":false},"INITIAL":{"rules":[0,1,28],"inclusive":true}};
return lexer;})()
parser.lexer = lexer;
function Parser () { this.yy = {}; }Parser.prototype = parser;parser.Parser = Parser;
return new Parser;
})();
if (typeof require !== 'undefined' && typeof exports !== 'undefined') {
exports.parser = handlebars;
exports.Parser = handlebars.Parser;
exports.parse = function () { return handlebars.parse.apply(handlebars, arguments); }
exports.main = function commonjsMain(args) {
		if (!args[1])
				throw new Error('Usage: '+args[0]+' FILE');
		var source, cwd;
		if (typeof process !== 'undefined') {
				source = require('fs').readFileSync(require('path').resolve(args[1]), "utf8");
		} else {
				source = require("file").path(require("file").cwd()).join(args[1]).read({charset: "utf-8"});
		}
		return exports.parser.parse(source);
}
if (typeof module !== 'undefined' && require.main === module) {
	exports.main(typeof process !== 'undefined' ? process.argv.slice(1) : require("system").args);
}
};
;
// lib/handlebars/compiler/base.js
Handlebars.Parser = handlebars;

Handlebars.parse = function(string) {
	Handlebars.Parser.yy = Handlebars.AST;
	return Handlebars.Parser.parse(string);
};

Handlebars.print = function(ast) {
	return new Handlebars.PrintVisitor().accept(ast);
};

Handlebars.logger = {
	DEBUG: 0, INFO: 1, WARN: 2, ERROR: 3, level: 3,

	// override in the host environment
	log: function(level, str) {}
};

Handlebars.log = function(level, str) { Handlebars.logger.log(level, str); };
;
// lib/handlebars/compiler/ast.js
(function() {

	Handlebars.AST = {};

	Handlebars.AST.ProgramNode = function(statements, inverse) {
		this.type = "program";
		this.statements = statements;
		if(inverse) { this.inverse = new Handlebars.AST.ProgramNode(inverse); }
	};

	Handlebars.AST.MustacheNode = function(rawParams, hash, unescaped) {
		this.type = "mustache";
		this.escaped = !unescaped;
		this.hash = hash;

		var id = this.id = rawParams[0];
		var params = this.params = rawParams.slice(1);

		// a mustache is an eligible helper if:
		// * its id is simple (a single part, not `this` or `..`)
		var eligibleHelper = this.eligibleHelper = id.isSimple;

		// a mustache is definitely a helper if:
		// * it is an eligible helper, and
		// * it has at least one parameter or hash segment
		this.isHelper = eligibleHelper && (params.length || hash);

		// if a mustache is an eligible helper but not a definite
		// helper, it is ambiguous, and will be resolved in a later
		// pass or at runtime.
	};

	Handlebars.AST.PartialNode = function(id, context) {
		this.type    = "partial";

		// TODO: disallow complex IDs

		this.id      = id;
		this.context = context;
	};

	var verifyMatch = function(open, close) {
		if(open.original !== close.original) {
			throw new Handlebars.Exception(open.original + " doesn't match " + close.original);
		}
	};

	Handlebars.AST.BlockNode = function(mustache, program, inverse, close) {
		verifyMatch(mustache.id, close);
		this.type = "block";
		this.mustache = mustache;
		this.program  = program;
		this.inverse  = inverse;

		if (this.inverse && !this.program) {
			this.isInverse = true;
		}
	};

	Handlebars.AST.ContentNode = function(string) {
		this.type = "content";
		this.string = string;
	};

	Handlebars.AST.HashNode = function(pairs) {
		this.type = "hash";
		this.pairs = pairs;
	};

	Handlebars.AST.IdNode = function(parts) {
		this.type = "ID";
		this.original = parts.join(".");

		var dig = [], depth = 0;

		for(var i=0,l=parts.length; i<l; i++) {
			var part = parts[i];

			if(part === "..") { depth++; }
			else if(part === "." || part === "this") { this.isScoped = true; }
			else { dig.push(part); }
		}

		this.parts    = dig;
		this.string   = dig.join('.');
		this.depth    = depth;

		// an ID is simple if it only has one part, and that part is not
		// `..` or `this`.
		this.isSimple = parts.length === 1 && !this.isScoped && depth === 0;
	};

	Handlebars.AST.DataNode = function(id) {
		this.type = "DATA";
		this.id = id;
	};

	Handlebars.AST.StringNode = function(string) {
		this.type = "STRING";
		this.string = string;
	};

	Handlebars.AST.IntegerNode = function(integer) {
		this.type = "INTEGER";
		this.integer = integer;
	};

	Handlebars.AST.BooleanNode = function(bool) {
		this.type = "BOOLEAN";
		this.bool = bool;
	};

	Handlebars.AST.CommentNode = function(comment) {
		this.type = "comment";
		this.comment = comment;
	};

})();;
// lib/handlebars/utils.js
Handlebars.Exception = function(message) {
	var tmp = Error.prototype.constructor.apply(this, arguments);

	for (var p in tmp) {
		if (tmp.hasOwnProperty(p)) { this[p] = tmp[p]; }
	}

	this.message = tmp.message;
};
Handlebars.Exception.prototype = new Error();

// Build out our basic SafeString type
Handlebars.SafeString = function(string) {
	this.string = string;
};
Handlebars.SafeString.prototype.toString = function() {
	return this.string.toString();
};

(function() {
	var escape = {
		"&": "&amp;",
		"<": "&lt;",
		">": "&gt;",
		'"': "&quot;",
		"'": "&#x27;",
		"`": "&#x60;"
	};

	var badChars = /[&<>"'`]/g;
	var possible = /[&<>"'`]/;

	var escapeChar = function(chr) {
		return escape[chr] || "&amp;";
	};

	Handlebars.Utils = {
		escapeExpression: function(string) {
			// don't escape SafeStrings, since they're already safe
			if (string instanceof Handlebars.SafeString) {
				return string.toString();
			} else if (string == null || string === false) {
				return "";
			}

			if(!possible.test(string)) { return string; }
			return string.replace(badChars, escapeChar);
		},

		isEmpty: function(value) {
			if (typeof value === "undefined") {
				return true;
			} else if (value === null) {
				return true;
			} else if (value === false) {
				return true;
			} else if(Object.prototype.toString.call(value) === "[object Array]" && value.length === 0) {
				return true;
			} else {
				return false;
			}
		}
	};
})();;
// lib/handlebars/compiler/compiler.js

/*jshint eqnull:true*/
Handlebars.Compiler = function() {};
Handlebars.JavaScriptCompiler = function() {};

(function(Compiler, JavaScriptCompiler) {
	// the foundHelper register will disambiguate helper lookup from finding a
	// function in a context. This is necessary for mustache compatibility, which
	// requires that context functions in blocks are evaluated by blockHelperMissing,
	// and then proceed as if the resulting value was provided to blockHelperMissing.

	Compiler.prototype = {
		compiler: Compiler,

		disassemble: function() {
			var opcodes = this.opcodes, opcode, out = [], params, param;

			for (var i=0, l=opcodes.length; i<l; i++) {
				opcode = opcodes[i];

				if (opcode.opcode === 'DECLARE') {
					out.push("DECLARE " + opcode.name + "=" + opcode.value);
				} else {
					params = [];
					for (var j=0; j<opcode.args.length; j++) {
						param = opcode.args[j];
						if (typeof param === "string") {
							param = "\"" + param.replace("\n", "\\n") + "\"";
						}
						params.push(param);
					}
					out.push(opcode.opcode + " " + params.join(" "));
				}
			}

			return out.join("\n");
		},

		guid: 0,

		compile: function(program, options) {
			this.children = [];
			this.depths = {list: []};
			this.options = options;

			// These changes will propagate to the other compiler components
			var knownHelpers = this.options.knownHelpers;
			this.options.knownHelpers = {
				'helperMissing': true,
				'blockHelperMissing': true,
				'each': true,
				'if': true,
				'unless': true,
				'with': true,
				'log': true
			};
			if (knownHelpers) {
				for (var name in knownHelpers) {
					this.options.knownHelpers[name] = knownHelpers[name];
				}
			}

			return this.program(program);
		},

		accept: function(node) {
			return this[node.type](node);
		},

		program: function(program) {
			var statements = program.statements, statement;
			this.opcodes = [];

			for(var i=0, l=statements.length; i<l; i++) {
				statement = statements[i];
				this[statement.type](statement);
			}
			this.isSimple = l === 1;

			this.depths.list = this.depths.list.sort(function(a, b) {
				return a - b;
			});

			return this;
		},

		compileProgram: function(program) {
			var result = new this.compiler().compile(program, this.options);
			var guid = this.guid++, depth;

			this.usePartial = this.usePartial || result.usePartial;

			this.children[guid] = result;

			for(var i=0, l=result.depths.list.length; i<l; i++) {
				depth = result.depths.list[i];

				if(depth < 2) { continue; }
				else { this.addDepth(depth - 1); }
			}

			return guid;
		},

		block: function(block) {
			var mustache = block.mustache,
					program = block.program,
					inverse = block.inverse;

			if (program) {
				program = this.compileProgram(program);
			}

			if (inverse) {
				inverse = this.compileProgram(inverse);
			}

			var type = this.classifyMustache(mustache);

			if (type === "helper") {
				this.helperMustache(mustache, program, inverse);
			} else if (type === "simple") {
				this.simpleMustache(mustache);

				// now that the simple mustache is resolved, we need to
				// evaluate it by executing `blockHelperMissing`
				this.opcode('pushProgram', program);
				this.opcode('pushProgram', inverse);
				this.opcode('pushLiteral', '{}');
				this.opcode('blockValue');
			} else {
				this.ambiguousMustache(mustache, program, inverse);

				// now that the simple mustache is resolved, we need to
				// evaluate it by executing `blockHelperMissing`
				this.opcode('pushProgram', program);
				this.opcode('pushProgram', inverse);
				this.opcode('pushLiteral', '{}');
				this.opcode('ambiguousBlockValue');
			}

			this.opcode('append');
		},

		hash: function(hash) {
			var pairs = hash.pairs, pair, val;

			this.opcode('push', '{}');

			for(var i=0, l=pairs.length; i<l; i++) {
				pair = pairs[i];
				val  = pair[1];

				this.accept(val);
				this.opcode('assignToHash', pair[0]);
			}
		},

		partial: function(partial) {
			var id = partial.id;
			this.usePartial = true;

			if(partial.context) {
				this.ID(partial.context);
			} else {
				this.opcode('push', 'depth0');
			}

			this.opcode('invokePartial', id.original);
			this.opcode('append');
		},

		content: function(content) {
			this.opcode('appendContent', content.string);
		},

		mustache: function(mustache) {
			var options = this.options;
			var type = this.classifyMustache(mustache);

			if (type === "simple") {
				this.simpleMustache(mustache);
			} else if (type === "helper") {
				this.helperMustache(mustache);
			} else {
				this.ambiguousMustache(mustache);
			}

			if(mustache.escaped && !options.noEscape) {
				this.opcode('appendEscaped');
			} else {
				this.opcode('append');
			}
		},

		ambiguousMustache: function(mustache, program, inverse) {
			var id = mustache.id, name = id.parts[0];

			this.opcode('getContext', id.depth);

			this.opcode('pushProgram', program);
			this.opcode('pushProgram', inverse);

			this.opcode('invokeAmbiguous', name);
		},

		simpleMustache: function(mustache, program, inverse) {
			var id = mustache.id;

			if (id.type === 'DATA') {
				this.DATA(id);
			} else if (id.parts.length) {
				this.ID(id);
			} else {
				// Simplified ID for `this`
				this.addDepth(id.depth);
				this.opcode('getContext', id.depth);
				this.opcode('pushContext');
			}

			this.opcode('resolvePossibleLambda');
		},

		helperMustache: function(mustache, program, inverse) {
			var params = this.setupFullMustacheParams(mustache, program, inverse),
					name = mustache.id.parts[0];

			if (this.options.knownHelpers[name]) {
				this.opcode('invokeKnownHelper', params.length, name);
			} else if (this.knownHelpersOnly) {
				throw new Error("You specified knownHelpersOnly, but used the unknown helper " + name);
			} else {
				this.opcode('invokeHelper', params.length, name);
			}
		},

		ID: function(id) {
			this.addDepth(id.depth);
			this.opcode('getContext', id.depth);

			var name = id.parts[0];
			if (!name) {
				this.opcode('pushContext');
			} else {
				this.opcode('lookupOnContext', id.parts[0]);
			}

			for(var i=1, l=id.parts.length; i<l; i++) {
				this.opcode('lookup', id.parts[i]);
			}
		},

		DATA: function(data) {
			this.options.data = true;
			this.opcode('lookupData', data.id);
		},

		STRING: function(string) {
			this.opcode('pushString', string.string);
		},

		INTEGER: function(integer) {
			this.opcode('pushLiteral', integer.integer);
		},

		BOOLEAN: function(bool) {
			this.opcode('pushLiteral', bool.bool);
		},

		comment: function() {},

		// HELPERS
		opcode: function(name) {
			this.opcodes.push({ opcode: name, args: [].slice.call(arguments, 1) });
		},

		declare: function(name, value) {
			this.opcodes.push({ opcode: 'DECLARE', name: name, value: value });
		},

		addDepth: function(depth) {
			if(isNaN(depth)) { throw new Error("EWOT"); }
			if(depth === 0) { return; }

			if(!this.depths[depth]) {
				this.depths[depth] = true;
				this.depths.list.push(depth);
			}
		},

		classifyMustache: function(mustache) {
			var isHelper   = mustache.isHelper;
			var isEligible = mustache.eligibleHelper;
			var options    = this.options;

			// if ambiguous, we can possibly resolve the ambiguity now
			if (isEligible && !isHelper) {
				var name = mustache.id.parts[0];

				if (options.knownHelpers[name]) {
					isHelper = true;
				} else if (options.knownHelpersOnly) {
					isEligible = false;
				}
			}

			if (isHelper) { return "helper"; }
			else if (isEligible) { return "ambiguous"; }
			else { return "simple"; }
		},

		pushParams: function(params) {
			var i = params.length, param;

			while(i--) {
				param = params[i];

				if(this.options.stringParams) {
					if(param.depth) {
						this.addDepth(param.depth);
					}

					this.opcode('getContext', param.depth || 0);
					this.opcode('pushStringParam', param.string);
				} else {
					this[param.type](param);
				}
			}
		},

		setupMustacheParams: function(mustache) {
			var params = mustache.params;
			this.pushParams(params);

			if(mustache.hash) {
				this.hash(mustache.hash);
			} else {
				this.opcode('pushLiteral', '{}');
			}

			return params;
		},

		// this will replace setupMustacheParams when we're done
		setupFullMustacheParams: function(mustache, program, inverse) {
			var params = mustache.params;
			this.pushParams(params);

			this.opcode('pushProgram', program);
			this.opcode('pushProgram', inverse);

			if(mustache.hash) {
				this.hash(mustache.hash);
			} else {
				this.opcode('pushLiteral', '{}');
			}

			return params;
		}
	};

	var Literal = function(value) {
		this.value = value;
	};

	JavaScriptCompiler.prototype = {
		// PUBLIC API: You can override these methods in a subclass to provide
		// alternative compiled forms for name lookup and buffering semantics
		nameLookup: function(parent, name, type) {
			if (/^[0-9]+$/.test(name)) {
				return parent + "[" + name + "]";
			} else if (JavaScriptCompiler.isValidJavaScriptVariableName(name)) {
				return parent + "." + name;
			}
			else {
				return parent + "['" + name + "']";
			}
		},

		appendToBuffer: function(string) {
			if (this.environment.isSimple) {
				return "return " + string + ";";
			} else {
				return "buffer += " + string + ";";
			}
		},

		initializeBuffer: function() {
			return this.quotedString("");
		},

		namespace: "Handlebars",
		// END PUBLIC API

		compile: function(environment, options, context, asObject) {
			this.environment = environment;
			this.options = options || {};

			Handlebars.log(Handlebars.logger.DEBUG, this.environment.disassemble() + "\n\n");

			this.name = this.environment.name;
			this.isChild = !!context;
			this.context = context || {
				programs: [],
				aliases: { }
			};

			this.preamble();

			this.stackSlot = 0;
			this.stackVars = [];
			this.registers = { list: [] };
			this.compileStack = [];

			this.compileChildren(environment, options);

			var opcodes = environment.opcodes, opcode;

			this.i = 0;

			for(l=opcodes.length; this.i<l; this.i++) {
				opcode = opcodes[this.i];

				if(opcode.opcode === 'DECLARE') {
					this[opcode.name] = opcode.value;
				} else {
					this[opcode.opcode].apply(this, opcode.args);
				}
			}

			return this.createFunctionContext(asObject);
		},

		nextOpcode: function() {
			var opcodes = this.environment.opcodes, opcode = opcodes[this.i + 1];
			return opcodes[this.i + 1];
		},

		eat: function(opcode) {
			this.i = this.i + 1;
		},

		preamble: function() {
			var out = [];

			if (!this.isChild) {
				var namespace = this.namespace;
				var copies = "helpers = helpers || " + namespace + ".helpers;";
				if (this.environment.usePartial) { copies = copies + " partials = partials || " + namespace + ".partials;"; }
				if (this.options.data) { copies = copies + " data = data || {};"; }
				out.push(copies);
			} else {
				out.push('');
			}

			if (!this.environment.isSimple) {
				out.push(", buffer = " + this.initializeBuffer());
			} else {
				out.push("");
			}

			// track the last context pushed into place to allow skipping the
			// getContext opcode when it would be a noop
			this.lastContext = 0;
			this.source = out;
		},

		createFunctionContext: function(asObject) {
			var locals = this.stackVars.concat(this.registers.list);

			if(locals.length > 0) {
				this.source[1] = this.source[1] + ", " + locals.join(", ");
			}

			// Generate minimizer alias mappings
			if (!this.isChild) {
				var aliases = [];
				for (var alias in this.context.aliases) {
					this.source[1] = this.source[1] + ', ' + alias + '=' + this.context.aliases[alias];
				}
			}

			if (this.source[1]) {
				this.source[1] = "var " + this.source[1].substring(2) + ";";
			}

			// Merge children
			if (!this.isChild) {
				this.source[1] += '\n' + this.context.programs.join('\n') + '\n';
			}

			if (!this.environment.isSimple) {
				this.source.push("return buffer;");
			}

			var params = this.isChild ? ["depth0", "data"] : ["Handlebars", "depth0", "helpers", "partials", "data"];

			for(var i=0, l=this.environment.depths.list.length; i<l; i++) {
				params.push("depth" + this.environment.depths.list[i]);
			}

			if (asObject) {
				params.push(this.source.join("\n  "));

				return Function.apply(this, params);
			} else {
				var functionSource = 'function ' + (this.name || '') + '(' + params.join(',') + ') {\n  ' + this.source.join("\n  ") + '}';
				Handlebars.log(Handlebars.logger.DEBUG, functionSource + "\n\n");
				return functionSource;
			}
		},

		// [blockValue]
		//
		// On stack, before: hash, inverse, program, value
		// On stack, after: return value of blockHelperMissing
		//
		// The purpose of this opcode is to take a block of the form
		// `{{#foo}}...{{/foo}}`, resolve the value of `foo`, and
		// replace it on the stack with the result of properly
		// invoking blockHelperMissing.
		blockValue: function() {
			this.context.aliases.blockHelperMissing = 'helpers.blockHelperMissing';

			var params = ["depth0"];
			this.setupParams(0, params);

			this.replaceStack(function(current) {
				params.splice(1, 0, current);
				return current + " = blockHelperMissing.call(" + params.join(", ") + ")";
			});
		},

		// [ambiguousBlockValue]
		//
		// On stack, before: hash, inverse, program, value
		// Compiler value, before: lastHelper=value of last found helper, if any
		// On stack, after, if no lastHelper: same as [blockValue]
		// On stack, after, if lastHelper: value
		ambiguousBlockValue: function() {
			this.context.aliases.blockHelperMissing = 'helpers.blockHelperMissing';

			var params = ["depth0"];
			this.setupParams(0, params);

			var current = this.topStack();
			params.splice(1, 0, current);

			this.source.push("if (!" + this.lastHelper + ") { " + current + " = blockHelperMissing.call(" + params.join(", ") + "); }");
		},

		// [appendContent]
		//
		// On stack, before: ...
		// On stack, after: ...
		//
		// Appends the string value of `content` to the current buffer
		appendContent: function(content) {
			this.source.push(this.appendToBuffer(this.quotedString(content)));
		},

		// [append]
		//
		// On stack, before: value, ...
		// On stack, after: ...
		//
		// Coerces `value` to a String and appends it to the current buffer.
		//
		// If `value` is truthy, or 0, it is coerced into a string and appended
		// Otherwise, the empty string is appended
		append: function() {
			var local = this.popStack();
			this.source.push("if(" + local + " || " + local + " === 0) { " + this.appendToBuffer(local) + " }");
			if (this.environment.isSimple) {
				this.source.push("else { " + this.appendToBuffer("''") + " }");
			}
		},

		// [appendEscaped]
		//
		// On stack, before: value, ...
		// On stack, after: ...
		//
		// Escape `value` and append it to the buffer
		appendEscaped: function() {
			var opcode = this.nextOpcode(), extra = "";
			this.context.aliases.escapeExpression = 'this.escapeExpression';

			if(opcode && opcode.opcode === 'appendContent') {
				extra = " + " + this.quotedString(opcode.args[0]);
				this.eat(opcode);
			}

			this.source.push(this.appendToBuffer("escapeExpression(" + this.popStack() + ")" + extra));
		},

		// [getContext]
		//
		// On stack, before: ...
		// On stack, after: ...
		// Compiler value, after: lastContext=depth
		//
		// Set the value of the `lastContext` compiler value to the depth
		getContext: function(depth) {
			if(this.lastContext !== depth) {
				this.lastContext = depth;
			}
		},

		// [lookupOnContext]
		//
		// On stack, before: ...
		// On stack, after: currentContext[name], ...
		//
		// Looks up the value of `name` on the current context and pushes
		// it onto the stack.
		lookupOnContext: function(name) {
			this.pushStack(this.nameLookup('depth' + this.lastContext, name, 'context'));
		},

		// [pushContext]
		//
		// On stack, before: ...
		// On stack, after: currentContext, ...
		//
		// Pushes the value of the current context onto the stack.
		pushContext: function() {
			this.pushStackLiteral('depth' + this.lastContext);
		},

		// [resolvePossibleLambda]
		//
		// On stack, before: value, ...
		// On stack, after: resolved value, ...
		//
		// If the `value` is a lambda, replace it on the stack by
		// the return value of the lambda
		resolvePossibleLambda: function() {
			this.context.aliases.functionType = '"function"';

			this.replaceStack(function(current) {
				return "typeof " + current + " === functionType ? " + current + "() : " + current;
			});
		},

		// [lookup]
		//
		// On stack, before: value, ...
		// On stack, after: value[name], ...
		//
		// Replace the value on the stack with the result of looking
		// up `name` on `value`
		lookup: function(name) {
			this.replaceStack(function(current) {
				return current + " == null || " + current + " === false ? " + current + " : " + this.nameLookup(current, name, 'context');
			});
		},

		// [lookupData]
		//
		// On stack, before: ...
		// On stack, after: data[id], ...
		//
		// Push the result of looking up `id` on the current data
		lookupData: function(id) {
			this.pushStack(this.nameLookup('data', id, 'data'));
		},

		// [pushStringParam]
		//
		// On stack, before: ...
		// On stack, after: string, currentContext, ...
		//
		// This opcode is designed for use in string mode, which
		// provides the string value of a parameter along with its
		// depth rather than resolving it immediately.
		pushStringParam: function(string) {
			this.pushStackLiteral('depth' + this.lastContext);
			this.pushString(string);
		},

		// [pushString]
		//
		// On stack, before: ...
		// On stack, after: quotedString(string), ...
		//
		// Push a quoted version of `string` onto the stack
		pushString: function(string) {
			this.pushStackLiteral(this.quotedString(string));
		},

		// [push]
		//
		// On stack, before: ...
		// On stack, after: expr, ...
		//
		// Push an expression onto the stack
		push: function(expr) {
			this.pushStack(expr);
		},

		// [pushLiteral]
		//
		// On stack, before: ...
		// On stack, after: value, ...
		//
		// Pushes a value onto the stack. This operation prevents
		// the compiler from creating a temporary variable to hold
		// it.
		pushLiteral: function(value) {
			this.pushStackLiteral(value);
		},

		// [pushProgram]
		//
		// On stack, before: ...
		// On stack, after: program(guid), ...
		//
		// Push a program expression onto the stack. This takes
		// a compile-time guid and converts it into a runtime-accessible
		// expression.
		pushProgram: function(guid) {
			if (guid != null) {
				this.pushStackLiteral(this.programExpression(guid));
			} else {
				this.pushStackLiteral(null);
			}
		},

		// [invokeHelper]
		//
		// On stack, before: hash, inverse, program, params..., ...
		// On stack, after: result of helper invocation
		//
		// Pops off the helper's parameters, invokes the helper,
		// and pushes the helper's return value onto the stack.
		//
		// If the helper is not found, `helperMissing` is called.
		invokeHelper: function(paramSize, name) {
			this.context.aliases.helperMissing = 'helpers.helperMissing';

			var helper = this.lastHelper = this.setupHelper(paramSize, name);
			this.register('foundHelper', helper.name);

			this.pushStack("foundHelper ? foundHelper.call(" +
				helper.callParams + ") " + ": helperMissing.call(" +
				helper.helperMissingParams + ")");
		},

		// [invokeKnownHelper]
		//
		// On stack, before: hash, inverse, program, params..., ...
		// On stack, after: result of helper invocation
		//
		// This operation is used when the helper is known to exist,
		// so a `helperMissing` fallback is not required.
		invokeKnownHelper: function(paramSize, name) {
			var helper = this.setupHelper(paramSize, name);
			this.pushStack(helper.name + ".call(" + helper.callParams + ")");
		},

		// [invokeAmbiguous]
		//
		// On stack, before: hash, inverse, program, params..., ...
		// On stack, after: result of disambiguation
		//
		// This operation is used when an expression like `{{foo}}`
		// is provided, but we don't know at compile-time whether it
		// is a helper or a path.
		//
		// This operation emits more code than the other options,
		// and can be avoided by passing the `knownHelpers` and
		// `knownHelpersOnly` flags at compile-time.
		invokeAmbiguous: function(name) {
			this.context.aliases.functionType = '"function"';

			this.pushStackLiteral('{}');
			var helper = this.setupHelper(0, name);

			var helperName = this.lastHelper = this.nameLookup('helpers', name, 'helper');
			this.register('foundHelper', helperName);

			var nonHelper = this.nameLookup('depth' + this.lastContext, name, 'context');
			var nextStack = this.nextStack();

			this.source.push('if (foundHelper) { ' + nextStack + ' = foundHelper.call(' + helper.callParams + '); }');
			this.source.push('else { ' + nextStack + ' = ' + nonHelper + '; ' + nextStack + ' = typeof ' + nextStack + ' === functionType ? ' + nextStack + '() : ' + nextStack + '; }');
		},

		// [invokePartial]
		//
		// On stack, before: context, ...
		// On stack after: result of partial invocation
		//
		// This operation pops off a context, invokes a partial with that context,
		// and pushes the result of the invocation back.
		invokePartial: function(name) {
			var params = [this.nameLookup('partials', name, 'partial'), "'" + name + "'", this.popStack(), "helpers", "partials"];

			if (this.options.data) {
				params.push("data");
			}

			this.context.aliases.self = "this";
			this.pushStack("self.invokePartial(" + params.join(", ") + ");");
		},

		// [assignToHash]
		//
		// On stack, before: value, hash, ...
		// On stack, after: hash, ...
		//
		// Pops a value and hash off the stack, assigns `hash[key] = value`
		// and pushes the hash back onto the stack.
		assignToHash: function(key) {
			var value = this.popStack();
			var hash = this.topStack();

			this.source.push(hash + "['" + key + "'] = " + value + ";");
		},

		// HELPERS

		compiler: JavaScriptCompiler,

		compileChildren: function(environment, options) {
			var children = environment.children, child, compiler;

			for(var i=0, l=children.length; i<l; i++) {
				child = children[i];
				compiler = new this.compiler();

				this.context.programs.push('');     // Placeholder to prevent name conflicts for nested children
				var index = this.context.programs.length;
				child.index = index;
				child.name = 'program' + index;
				this.context.programs[index] = compiler.compile(child, options, this.context);
			}
		},

		programExpression: function(guid) {
			this.context.aliases.self = "this";

			if(guid == null) {
				return "self.noop";
			}

			var child = this.environment.children[guid],
					depths = child.depths.list, depth;

			var programParams = [child.index, child.name, "data"];

			for(var i=0, l = depths.length; i<l; i++) {
				depth = depths[i];

				if(depth === 1) { programParams.push("depth0"); }
				else { programParams.push("depth" + (depth - 1)); }
			}

			if(depths.length === 0) {
				return "self.program(" + programParams.join(", ") + ")";
			} else {
				programParams.shift();
				return "self.programWithDepth(" + programParams.join(", ") + ")";
			}
		},

		register: function(name, val) {
			this.useRegister(name);
			this.source.push(name + " = " + val + ";");
		},

		useRegister: function(name) {
			if(!this.registers[name]) {
				this.registers[name] = true;
				this.registers.list.push(name);
			}
		},

		pushStackLiteral: function(item) {
			this.compileStack.push(new Literal(item));
			return item;
		},

		pushStack: function(item) {
			this.source.push(this.incrStack() + " = " + item + ";");
			this.compileStack.push("stack" + this.stackSlot);
			return "stack" + this.stackSlot;
		},

		replaceStack: function(callback) {
			var item = callback.call(this, this.topStack());

			this.source.push(this.topStack() + " = " + item + ";");
			return "stack" + this.stackSlot;
		},

		nextStack: function(skipCompileStack) {
			var name = this.incrStack();
			this.compileStack.push("stack" + this.stackSlot);
			return name;
		},

		incrStack: function() {
			this.stackSlot++;
			if(this.stackSlot > this.stackVars.length) { this.stackVars.push("stack" + this.stackSlot); }
			return "stack" + this.stackSlot;
		},

		popStack: function() {
			var item = this.compileStack.pop();

			if (item instanceof Literal) {
				return item.value;
			} else {
				this.stackSlot--;
				return item;
			}
		},

		topStack: function() {
			var item = this.compileStack[this.compileStack.length - 1];

			if (item instanceof Literal) {
				return item.value;
			} else {
				return item;
			}
		},

		quotedString: function(str) {
			return '"' + str
				.replace(/\\/g, '\\\\')
				.replace(/"/g, '\\"')
				.replace(/\n/g, '\\n')
				.replace(/\r/g, '\\r') + '"';
		},

		setupHelper: function(paramSize, name) {
			var params = [];
			this.setupParams(paramSize, params);
			var foundHelper = this.nameLookup('helpers', name, 'helper');

			return {
				params: params,
				name: foundHelper,
				callParams: ["depth0"].concat(params).join(", "),
				helperMissingParams: ["depth0", this.quotedString(name)].concat(params).join(", ")
			};
		},

		// the params and contexts arguments are passed in arrays
		// to fill in
		setupParams: function(paramSize, params) {
			var options = [], contexts = [], param, inverse, program;

			options.push("hash:" + this.popStack());

			inverse = this.popStack();
			program = this.popStack();

			// Avoid setting fn and inverse if neither are set. This allows
			// helpers to do a check for `if (options.fn)`
			if (program || inverse) {
				if (!program) {
					this.context.aliases.self = "this";
					program = "self.noop";
				}

				if (!inverse) {
				 this.context.aliases.self = "this";
					inverse = "self.noop";
				}

				options.push("inverse:" + inverse);
				options.push("fn:" + program);
			}

			for(var i=0; i<paramSize; i++) {
				param = this.popStack();
				params.push(param);

				if(this.options.stringParams) {
					contexts.push(this.popStack());
				}
			}

			if (this.options.stringParams) {
				options.push("contexts:[" + contexts.join(",") + "]");
			}

			if(this.options.data) {
				options.push("data:data");
			}

			params.push("{" + options.join(",") + "}");
			return params.join(", ");
		}
	};

	var reservedWords = (
		"break else new var" +
		" case finally return void" +
		" catch for switch while" +
		" continue function this with" +
		" default if throw" +
		" delete in try" +
		" do instanceof typeof" +
		" abstract enum int short" +
		" boolean export interface static" +
		" byte extends long super" +
		" char final native synchronized" +
		" class float package throws" +
		" const goto private transient" +
		" debugger implements protected volatile" +
		" double import public let yield"
	).split(" ");

	var compilerWords = JavaScriptCompiler.RESERVED_WORDS = {};

	for(var i=0, l=reservedWords.length; i<l; i++) {
		compilerWords[reservedWords[i]] = true;
	}

	JavaScriptCompiler.isValidJavaScriptVariableName = function(name) {
		if(!JavaScriptCompiler.RESERVED_WORDS[name] && /^[a-zA-Z_$][0-9a-zA-Z_$]+$/.test(name)) {
			return true;
		}
		return false;
	};

})(Handlebars.Compiler, Handlebars.JavaScriptCompiler);

Handlebars.precompile = function(string, options) {
	options = options || {};

	var ast = Handlebars.parse(string);
	var environment = new Handlebars.Compiler().compile(ast, options);
	return new Handlebars.JavaScriptCompiler().compile(environment, options);
};

Handlebars.compile = function(string, options) {
	options = options || {};

	var compiled;
	function compile() {
		var ast = Handlebars.parse(string);
		var environment = new Handlebars.Compiler().compile(ast, options);
		var templateSpec = new Handlebars.JavaScriptCompiler().compile(environment, options, undefined, true);
		return Handlebars.template(templateSpec);
	}

	// Template is only compiled on first use and cached after that point.
	return function(context, options) {
		if (!compiled) {
			compiled = compile();
		}
		return compiled.call(this, context, options);
	};
};
;
// lib/handlebars/runtime.js
Handlebars.VM = {
	template: function(templateSpec) {
		// Just add water
		var container = {
			escapeExpression: Handlebars.Utils.escapeExpression,
			invokePartial: Handlebars.VM.invokePartial,
			programs: [],
			program: function(i, fn, data) {
				var programWrapper = this.programs[i];
				if(data) {
					return Handlebars.VM.program(fn, data);
				} else if(programWrapper) {
					return programWrapper;
				} else {
					programWrapper = this.programs[i] = Handlebars.VM.program(fn);
					return programWrapper;
				}
			},
			programWithDepth: Handlebars.VM.programWithDepth,
			noop: Handlebars.VM.noop
		};

		return function(context, options) {
			options = options || {};
			return templateSpec.call(container, Handlebars, context, options.helpers, options.partials, options.data);
		};
	},

	programWithDepth: function(fn, data, $depth) {
		var args = Array.prototype.slice.call(arguments, 2);

		return function(context, options) {
			options = options || {};

			return fn.apply(this, [context, options.data || data].concat(args));
		};
	},
	program: function(fn, data) {
		return function(context, options) {
			options = options || {};

			return fn(context, options.data || data);
		};
	},
	noop: function() { return ""; },
	invokePartial: function(partial, name, context, helpers, partials, data) {
		var options = { helpers: helpers, partials: partials, data: data };

		if(partial === undefined) {
			throw new Handlebars.Exception("The partial " + name + " could not be found");
		} else if(partial instanceof Function) {
			return partial(context, options);
		} else if (!Handlebars.compile) {
			throw new Handlebars.Exception("The partial " + name + " could not be compiled when running in runtime-only mode");
		} else {
			partials[name] = Handlebars.compile(partial, {data: data !== undefined});
			return partials[name](context, options);
		}
	}
};

Handlebars.template = Handlebars.VM.template;

/*!
 * Lo-Dash v0.6.1 <http://lodash.com>
 * Copyright 2012 John-David Dalton <http://allyoucanleet.com/>
 * Based on Underscore.js 1.3.3, copyright 2009-2012 Jeremy Ashkenas, DocumentCloud Inc.
 * <http://documentcloud.github.com/underscore>
 * Available under MIT license <http://lodash.com/license>
 */
;(function(window, undefined) {
	'use strict';

	/**
	 * Used to cache the last `_.templateSettings.evaluate` delimiter to avoid
	 * unnecessarily assigning `reEvaluateDelimiter` a new generated regexp.
	 * Assigned in `_.template`.
	 */
	var lastEvaluateDelimiter;

	/**
	 * Used to cache the last template `options.variable` to avoid unnecessarily
	 * assigning `reDoubleVariable` a new generated regexp. Assigned in `_.template`.
	 */
	var lastVariable;

	/**
	 * Used to match potentially incorrect data object references, like `obj.obj`,
	 * in compiled templates. Assigned in `_.template`.
	 */
	var reDoubleVariable;

	/**
	 * Used to match "evaluate" delimiters, including internal delimiters,
	 * in template text. Assigned in `_.template`.
	 */
	var reEvaluateDelimiter;

	/** Detect free variable `exports` */
	var freeExports = typeof exports == 'object' && exports &&
		(typeof global == 'object' && global && global == global.global && (window = global), exports);

	/** Native prototype shortcuts */
	var ArrayProto = Array.prototype,
			BoolProto = Boolean.prototype,
			ObjectProto = Object.prototype,
			NumberProto = Number.prototype,
			StringProto = String.prototype;

	/** Used to generate unique IDs */
	var idCounter = 0;

	/** Used by `cachedContains` as the default size when optimizations are enabled for large arrays */
	var largeArraySize = 30;

	/** Used to restore the original `_` reference in `noConflict` */
	var oldDash = window._;

	/** Used to detect delimiter values that should be processed by `tokenizeEvaluate` */
	var reComplexDelimiter = /[-+=!~*%&^<>|{(\/]|\[\D|\b(?:delete|in|instanceof|new|typeof|void)\b/;

	/** Used to match HTML entities */
	var reEscapedHtml = /&(?:amp|lt|gt|quot|#x27);/g;

	/** Used to match empty string literals in compiled template source */
	var reEmptyStringLeading = /\b__p \+= '';/g,
			reEmptyStringMiddle = /\b(__p \+=) '' \+/g,
			reEmptyStringTrailing = /(__e\(.*?\)|\b__t\)) \+\n'';/g;

	/** Used to match regexp flags from their coerced string values */
	var reFlags = /\w*$/;

	/** Used to insert the data object variable into compiled template source */
	var reInsertVariable = /(?:__e|__t = )\(\s*(?![\d\s"']|this\.)/g;

	/** Used to detect if a method is native */
	var reNative = RegExp('^' +
		(ObjectProto.valueOf + '')
			.replace(/[.*+?^=!:${}()|[\]\/\\]/g, '\\$&')
			.replace(/valueOf|for [^\]]+/g, '.+?') + '$'
	);

	/** Used to match internally used tokens in template text */
	var reToken = /__token__(\d+)/g;

	/** Used to match HTML characters */
	var reUnescapedHtml = /[&<>"']/g;

	/** Used to match unescaped characters in compiled string literals */
	var reUnescapedString = /['\n\r\t\u2028\u2029\\]/g;

	/** Used to fix the JScript [[DontEnum]] bug */
	var shadowed = [
		'constructor', 'hasOwnProperty', 'isPrototypeOf', 'propertyIsEnumerable',
		'toLocaleString', 'toString', 'valueOf'
	];

	/** Used to make template sourceURLs easier to identify */
	var templateCounter = 0;

	/** Used to replace template delimiters */
	var token = '__token__';

	/** Used to store tokenized template text snippets */
	var tokenized = [];

	/** Native method shortcuts */
	var concat = ArrayProto.concat,
			hasOwnProperty = ObjectProto.hasOwnProperty,
			push = ArrayProto.push,
			propertyIsEnumerable = ObjectProto.propertyIsEnumerable,
			slice = ArrayProto.slice,
			toString = ObjectProto.toString;

	/* Native method shortcuts for methods with the same name as other `lodash` methods */
	var nativeBind = reNative.test(nativeBind = slice.bind) && nativeBind,
			nativeIsArray = reNative.test(nativeIsArray = Array.isArray) && nativeIsArray,
			nativeIsFinite = window.isFinite,
			nativeKeys = reNative.test(nativeKeys = Object.keys) && nativeKeys;

	/** `Object#toString` result shortcuts */
	var argsClass = '[object Arguments]',
			arrayClass = '[object Array]',
			boolClass = '[object Boolean]',
			dateClass = '[object Date]',
			funcClass = '[object Function]',
			numberClass = '[object Number]',
			objectClass = '[object Object]',
			regexpClass = '[object RegExp]',
			stringClass = '[object String]';

	/** Timer shortcuts */
	var clearTimeout = window.clearTimeout,
			setTimeout = window.setTimeout;

	/**
	 * Detect the JScript [[DontEnum]] bug:
	 *
	 * In IE < 9 an objects own properties, shadowing non-enumerable ones, are
	 * made non-enumerable as well.
	 */
	var hasDontEnumBug;

	/**
	 * Detect if `Array#shift` and `Array#splice` augment array-like objects
	 * incorrectly:
	 *
	 * Firefox < 10, IE compatibility mode, and IE < 9 have buggy Array `shift()`
	 * and `splice()` functions that fail to remove the last element, `value[0]`,
	 * of array-like objects even though the `length` property is set to `0`.
	 * The `shift()` method is buggy in IE 8 compatibility mode, while `splice()`
	 * is buggy regardless of mode in IE < 9 and buggy in compatibility mode in IE 9.
	 */
	var hasObjectSpliceBug;

	/** Detect if own properties are iterated after inherited properties (IE < 9) */
	var iteratesOwnLast;

	/** Detect if an `arguments` object's indexes are non-enumerable (IE < 9) */
	var noArgsEnum = true;

	(function() {
		var object = { '0': 1, 'length': 1 },
				props = [];

		function ctor() { this.x = 1; }
		ctor.prototype = { 'valueOf': 1, 'y': 1 };
		for (var prop in new ctor) { props.push(prop); }
		for (prop in arguments) { noArgsEnum = !prop; }

		hasDontEnumBug = (props + '').length < 4;
		iteratesOwnLast = props[0] != 'x';
		hasObjectSpliceBug = (props.splice.call(object, 0, 1), object[0]);
	}(1));

	/** Detect if an `arguments` object's [[Class]] is unresolvable (Firefox < 4, IE < 9) */
	var noArgsClass = !isArguments(arguments);

	/** Detect if `Array#slice` cannot be used to convert strings to arrays (Opera < 10.52) */
	var noArraySliceOnStrings = slice.call('x')[0] != 'x';

	/**
	 * Detect lack of support for accessing string characters by index:
	 *
	 * IE < 8 can't access characters by index and IE 8 can only access
	 * characters by index on string literals.
	 */
	var noCharByIndex = ('x'[0] + Object('x')[0]) != 'xx';

	/**
	 * Detect if a node's [[Class]] is unresolvable (IE < 9)
	 * and that the JS engine won't error when attempting to coerce an object to
	 * a string without a `toString` property value of `typeof` "function".
	 */
	try {
		var noNodeClass = ({ 'toString': 0 } + '', toString.call(window.document || 0) == objectClass);
	} catch(e) { }

	/* Detect if `Function#bind` exists and is inferred to be fast (all but V8) */
	var isBindFast = nativeBind && /\n|Opera/.test(nativeBind + toString.call(window.opera));

	/* Detect if `Object.keys` exists and is inferred to be fast (IE, Opera, V8) */
	var isKeysFast = nativeKeys && /^.+$|true/.test(nativeKeys + !!window.attachEvent);

	/* Detect if strict mode, "use strict", is inferred to be fast (V8) */
	var isStrictFast = !isBindFast;

	/**
	 * Detect if sourceURL syntax is usable without erroring:
	 *
	 * The JS engine in Adobe products, like InDesign, will throw a syntax error
	 * when it encounters a single line comment beginning with the `@` symbol.
	 *
	 * The JS engine in Narwhal will generate the function `function anonymous(){//}`
	 * and throw a syntax error.
	 *
	 * Avoid comments beginning `@` symbols in IE because they are part of its
	 * non-standard conditional compilation support.
	 * http://msdn.microsoft.com/en-us/library/121hztk3(v=vs.94).aspx
	 */
	try {
		var useSourceURL = (Function('//@')(), !window.attachEvent);
	} catch(e){ }

	/** Used to identify object classifications that are array-like */
	var arrayLikeClasses = {};
	arrayLikeClasses[boolClass] = arrayLikeClasses[dateClass] = arrayLikeClasses[funcClass] =
	arrayLikeClasses[numberClass] = arrayLikeClasses[objectClass] = arrayLikeClasses[regexpClass] = false;
	arrayLikeClasses[argsClass] = arrayLikeClasses[arrayClass] = arrayLikeClasses[stringClass] = true;

	/** Used to identify object classifications that `_.clone` supports */
	var cloneableClasses = {};
	cloneableClasses[argsClass] = cloneableClasses[funcClass] = false;
	cloneableClasses[arrayClass] = cloneableClasses[boolClass] = cloneableClasses[dateClass] =
	cloneableClasses[numberClass] = cloneableClasses[objectClass] = cloneableClasses[regexpClass] =
	cloneableClasses[stringClass] = true;

	/**
	 * Used to convert characters to HTML entities:
	 *
	 * Though the `>` character is escaped for symmetry, characters like `>` and `/`
	 * don't require escaping in HTML and have no special meaning unless they're part
	 * of a tag or an unquoted attribute value.
	 * http://mathiasbynens.be/notes/ambiguous-ampersands (under "semi-related fun fact")
	 */
	var htmlEscapes = {
		'&': '&amp;',
		'<': '&lt;',
		'>': '&gt;',
		'"': '&quot;',
		"'": '&#x27;'
	};

	/** Used to convert HTML entities to characters */
	var htmlUnescapes = {
		'&amp;': '&',
		'&lt;': '<',
		'&gt;': '>',
		'&quot;': '"',
		'&#x27;': "'"
	};

	/** Used to determine if values are of the language type Object */
	var objectTypes = {
		'boolean': false,
		'function': true,
		'object': true,
		'number': false,
		'string': false,
		'undefined': false,
		'unknown': true
	};

	/** Used to escape characters for inclusion in compiled string literals */
	var stringEscapes = {
		'\\': '\\',
		"'": "'",
		'\n': 'n',
		'\r': 'r',
		'\t': 't',
		'\u2028': 'u2028',
		'\u2029': 'u2029'
	};

	/*--------------------------------------------------------------------------*/

	/**
	 * The `lodash` function.
	 *
	 * @name _
	 * @constructor
	 * @param {Mixed} value The value to wrap in a `LoDash` instance.
	 * @returns {Object} Returns a `LoDash` instance.
	 */
	function lodash(value) {
		// allow invoking `lodash` without the `new` operator
		return new LoDash(value);
	}

	/**
	 * Creates a `LoDash` instance that wraps a value to allow chaining.
	 *
	 * @private
	 * @constructor
	 * @param {Mixed} value The value to wrap.
	 */
	function LoDash(value) {
		// exit early if already wrapped
		if (value && value._wrapped) {
			return value;
		}
		this._wrapped = value;
	}

	/**
	 * By default, the template delimiters used by Lo-Dash are similar to those in
	 * embedded Ruby (ERB). Change the following template settings to use alternative
	 * delimiters.
	 *
	 * @static
	 * @memberOf _
	 * @type Object
	 */
	lodash.templateSettings = {

		/**
		 * Used to detect `data` property values to be HTML-escaped.
		 *
		 * @static
		 * @memberOf _.templateSettings
		 * @type RegExp
		 */
		'escape': /<%-([\s\S]+?)%>/g,

		/**
		 * Used to detect code to be evaluated.
		 *
		 * @static
		 * @memberOf _.templateSettings
		 * @type RegExp
		 */
		'evaluate': /<%([\s\S]+?)%>/g,

		/**
		 * Used to detect `data` property values to inject.
		 *
		 * @static
		 * @memberOf _.templateSettings
		 * @type RegExp
		 */
		'interpolate': /<%=([\s\S]+?)%>/g,

		/**
		 * Used to reference the data object in the template text.
		 *
		 * @static
		 * @memberOf _.templateSettings
		 * @type String
		 */
		'variable': ''
	};

	/*--------------------------------------------------------------------------*/

	/**
	 * The template used to create iterator functions.
	 *
	 * @private
	 * @param {Obect} data The data object used to populate the text.
	 * @returns {String} Returns the interpolated text.
	 */
	var iteratorTemplate = template(
		// conditional strict mode
		'<% if (useStrict) { %>\'use strict\';\n<% } %>' +

		// the `iteratee` may be reassigned by the `top` snippet
		'var index, value, iteratee = <%= firstArg %>, ' +
		// assign the `result` variable an initial value
		'result<% if (init) { %> = <%= init %><% } %>;\n' +
		// add code to exit early or do so if the first argument is falsey
		'<%= exit %>;\n' +
		// add code after the exit snippet but before the iteration branches
		'<%= top %>;\n' +

		// the following branch is for iterating arrays and array-like objects
		'<% if (arrayBranch) { %>' +
		'var length = iteratee.length; index = -1;' +
		'  <% if (objectBranch) { %>\nif (length > -1 && length === length >>> 0) {<% } %>' +

		// add support for accessing string characters by index if needed
		'  <% if (noCharByIndex) { %>\n' +
		'  if (toString.call(iteratee) == stringClass) {\n' +
		'    iteratee = iteratee.split(\'\')\n' +
		'  }' +
		'  <% } %>\n' +

		'  <%= arrayBranch.beforeLoop %>;\n' +
		'  while (++index < length) {\n' +
		'    value = iteratee[index];\n' +
		'    <%= arrayBranch.inLoop %>\n' +
		'  }' +
		'  <% if (objectBranch) { %>\n}<% } %>' +
		'<% } %>' +

		// the following branch is for iterating an object's own/inherited properties
		'<% if (objectBranch) { %>' +
		'  <% if (arrayBranch) { %>\nelse {' +

		// add support for iterating over `arguments` objects if needed
		'  <%  } else if (noArgsEnum) { %>\n' +
		'  var length = iteratee.length; index = -1;\n' +
		'  if (length && isArguments(iteratee)) {\n' +
		'    while (++index < length) {\n' +
		'      value = iteratee[index += \'\'];\n' +
		'      <%= objectBranch.inLoop %>\n' +
		'    }\n' +
		'  } else {' +
		'  <% } %>' +

		// Firefox < 3.6, Opera > 9.50 - Opera < 11.60, and Safari < 5.1
		// (if the prototype or a property on the prototype has been set)
		// incorrectly sets a function's `prototype` property [[Enumerable]]
		// value to `true`. Because of this Lo-Dash standardizes on skipping
		// the the `prototype` property of functions regardless of its
		// [[Enumerable]] value.
		'  <% if (!hasDontEnumBug) { %>\n' +
		'  var skipProto = typeof iteratee == \'function\' && \n' +
		'    propertyIsEnumerable.call(iteratee, \'prototype\');\n' +
		'  <% } %>' +

		// iterate own properties using `Object.keys` if it's fast
		'  <% if (isKeysFast && useHas) { %>\n' +
		'  var ownIndex = -1,\n' +
		'      ownProps = objectTypes[typeof iteratee] ? nativeKeys(iteratee) : [],\n' +
		'      length = ownProps.length;\n\n' +
		'  <%= objectBranch.beforeLoop %>;\n' +
		'  while (++ownIndex < length) {\n' +
		'    index = ownProps[ownIndex];\n' +
		'    <% if (!hasDontEnumBug) { %>if (!(skipProto && index == \'prototype\')) {\n  <% } %>' +
		'    value = iteratee[index];\n' +
		'    <%= objectBranch.inLoop %>\n' +
		'    <% if (!hasDontEnumBug) { %>}\n<% } %>' +
		'  }' +

		// else using a for-in loop
		'  <% } else { %>\n' +
		'  <%= objectBranch.beforeLoop %>;\n' +
		'  for (index in iteratee) {' +
		'    <% if (!hasDontEnumBug || useHas) { %>\n    if (<%' +
		'      if (!hasDontEnumBug) { %>!(skipProto && index == \'prototype\')<% }' +
		'      if (!hasDontEnumBug && useHas) { %> && <% }' +
		'      if (useHas) { %>hasOwnProperty.call(iteratee, index)<% }' +
		'    %>) {' +
		'    <% } %>\n' +
		'    value = iteratee[index];\n' +
		'    <%= objectBranch.inLoop %>;\n' +
		'    <% if (!hasDontEnumBug || useHas) { %>}\n<% } %>' +
		'  }' +
		'  <% } %>' +

		// Because IE < 9 can't set the `[[Enumerable]]` attribute of an
		// existing property and the `constructor` property of a prototype
		// defaults to non-enumerable, Lo-Dash skips the `constructor`
		// property when it infers it's iterating over a `prototype` object.
		'  <% if (hasDontEnumBug) { %>\n\n' +
		'  var ctor = iteratee.constructor;\n' +
		'    <% for (var k = 0; k < 7; k++) { %>\n' +
		'  index = \'<%= shadowed[k] %>\';\n' +
		'  if (<%' +
		'      if (shadowed[k] == \'constructor\') {' +
		'        %>!(ctor && ctor.prototype === iteratee) && <%' +
		'      } %>hasOwnProperty.call(iteratee, index)) {\n' +
		'    value = iteratee[index];\n' +
		'    <%= objectBranch.inLoop %>\n' +
		'  }' +
		'    <% } %>' +
		'  <% } %>' +
		'  <% if (arrayBranch || noArgsEnum) { %>\n}<% } %>' +
		'<% } %>\n' +

		// add code to the bottom of the iteration function
		'<%= bottom %>;\n' +
		// finally, return the `result`
		'return result'
	);

	/**
	 * Reusable iterator options shared by
	 * `every`, `filter`, `find`, `forEach`, `forIn`, `forOwn`, `groupBy`, `map`,
	 * `reject`, `some`, and `sortBy`.
	 */
	var baseIteratorOptions = {
		'args': 'collection, callback, thisArg',
		'init': 'collection',
		'top':
			'if (!callback) {\n' +
			'  callback = identity\n' +
			'}\n' +
			'else if (thisArg) {\n' +
			'  callback = iteratorBind(callback, thisArg)\n' +
			'}',
		'inLoop': 'if (callback(value, index, collection) === false) return result'
	};

	/** Reusable iterator options for `countBy`, `groupBy`, and `sortBy` */
	var countByIteratorOptions = {
		'init': '{}',
		'top':
			'var prop;\n' +
			'if (typeof callback != \'function\') {\n' +
			'  var valueProp = callback;\n' +
			'  callback = function(value) { return value[valueProp] }\n' +
			'}\n' +
			'else if (thisArg) {\n' +
			'  callback = iteratorBind(callback, thisArg)\n' +
			'}',
		'inLoop':
			'prop = callback(value, index, collection);\n' +
			'(hasOwnProperty.call(result, prop) ? result[prop]++ : result[prop] = 1)'
	};

	/** Reusable iterator options for `drop` and `pick` */
	var dropIteratorOptions = {
		'useHas': false,
		'args': 'object, callback, thisArg',
		'init': '{}',
		'top':
			'var isFunc = typeof callback == \'function\';\n' +
			'if (!isFunc) {\n' +
			'  var props = concat.apply(ArrayProto, arguments)\n' +
			'} else if (thisArg) {\n' +
			'  callback = iteratorBind(callback, thisArg)\n' +
			'}',
		'inLoop':
			'if (isFunc\n' +
			'  ? !callback(value, index, object)\n' +
			'  : indexOf(props, index) < 0\n' +
			') result[index] = value'
	};

	/** Reusable iterator options for `every` and `some` */
	var everyIteratorOptions = {
		'init': 'true',
		'inLoop': 'if (!callback(value, index, collection)) return !result'
	};

	/** Reusable iterator options for `defaults` and `extend` */
	var extendIteratorOptions = {
		'useHas': false,
		'useStrict': false,
		'args': 'object',
		'init': 'object',
		'top':
			'for (var argsIndex = 1, argsLength = arguments.length; argsIndex < argsLength; argsIndex++) {\n' +
			'  if (iteratee = arguments[argsIndex]) {',
		'inLoop': 'result[index] = value',
		'bottom': '  }\n}'
	};

	/** Reusable iterator options for `filter`, `reject`, and `where` */
	var filterIteratorOptions = {
		'init': '[]',
		'inLoop': 'callback(value, index, collection) && result.push(value)'
	};

	/** Reusable iterator options for `find`, `forEach`, `forIn`, and `forOwn` */
	var forEachIteratorOptions = {
		'top': 'if (thisArg) callback = iteratorBind(callback, thisArg)'
	};

	/** Reusable iterator options for `forIn` and `forOwn` */
	var forOwnIteratorOptions = {
		'inLoop': {
			'object': baseIteratorOptions.inLoop
		}
	};

	/** Reusable iterator options for `invoke`, `map`, `pluck`, and `sortBy` */
	var mapIteratorOptions = {
		'init': '',
		'exit': 'if (!collection) return []',
		'beforeLoop': {
			'array':  'result = Array(length)',
			'object': 'result = ' + (isKeysFast ? 'Array(length)' : '[]')
		},
		'inLoop': {
			'array':  'result[index] = callback(value, index, collection)',
			'object': 'result' + (isKeysFast ? '[ownIndex] = ' : '.push') + '(callback(value, index, collection))'
		}
	};

	/*--------------------------------------------------------------------------*/

	/**
	 * Creates a new function optimized for searching large arrays for a given `value`,
	 * starting at `fromIndex`, using strict equality for comparisons, i.e. `===`.
	 *
	 * @private
	 * @param {Array} array The array to search.
	 * @param {Mixed} value The value to search for.
	 * @param {Number} [fromIndex=0] The index to start searching from.
	 * @param {Number} [largeSize=30] The length at which an array is considered large.
	 * @returns {Boolean} Returns `true` if `value` is found, else `false`.
	 */
	function cachedContains(array, fromIndex, largeSize) {
		fromIndex || (fromIndex = 0);

		var length = array.length,
				isLarge = (length - fromIndex) >= (largeSize || largeArraySize),
				cache = isLarge ? {} : array;

		if (isLarge) {
			// init value cache
			var key,
					index = fromIndex - 1;

			while (++index < length) {
				// manually coerce `value` to string because `hasOwnProperty`, in some
				// older versions of Firefox, coerces objects incorrectly
				key = array[index] + '';
				(hasOwnProperty.call(cache, key) ? cache[key] : (cache[key] = [])).push(array[index]);
			}
		}
		return function(value) {
			if (isLarge) {
				var key = value + '';
				return hasOwnProperty.call(cache, key) && indexOf(cache[key], value) > -1;
			}
			return indexOf(cache, value, fromIndex) > -1;
		}
	}

	/**
	 * Creates compiled iteration functions. The iteration function will be created
	 * to iterate over only objects if the first argument of `options.args` is
	 * "object" or `options.inLoop.array` is falsey.
	 *
	 * @private
	 * @param {Object} [options1, options2, ...] The compile options objects.
	 *
	 *  useHas - A boolean to specify whether or not to use `hasOwnProperty` checks
	 *   in the object loop.
	 *
	 *  useStrict - A boolean to specify whether or not to include the ES5
	 *   "use strict" directive.
	 *
	 *  args - A string of comma separated arguments the iteration function will
	 *   accept.
	 *
	 *  init - A string to specify the initial value of the `result` variable.
	 *
	 *  exit - A string of code to use in place of the default exit-early check
	 *   of `if (!arguments[0]) return result`.
	 *
	 *  top - A string of code to execute after the exit-early check but before
	 *   the iteration branches.
	 *
	 *  beforeLoop - A string or object containing an "array" or "object" property
	 *   of code to execute before the array or object loops.
	 *
	 *  inLoop - A string or object containing an "array" or "object" property
	 *   of code to execute in the array or object loops.
	 *
	 *  bottom - A string of code to execute after the iteration branches but
	 *   before the `result` is returned.
	 *
	 * @returns {Function} Returns the compiled function.
	 */
	function createIterator() {
		var object,
				prop,
				value,
				index = -1,
				length = arguments.length;

		// merge options into a template data object
		var data = {
			'bottom': '',
			'exit': '',
			'init': '',
			'top': '',
			'arrayBranch': { 'beforeLoop': '' },
			'objectBranch': { 'beforeLoop': '' }
		};

		while (++index < length) {
			object = arguments[index];
			for (prop in object) {
				value = (value = object[prop]) == null ? '' : value;
				// keep this regexp explicit for the build pre-process
				if (/beforeLoop|inLoop/.test(prop)) {
					if (typeof value == 'string') {
						value = { 'array': value, 'object': value };
					}
					data.arrayBranch[prop] = value.array || '';
					data.objectBranch[prop] = value.object || '';
				} else {
					data[prop] = value;
				}
			}
		}
		// set additional template `data` values
		var args = data.args,
				firstArg = /^[^,]+/.exec(args)[0],
				useStrict = data.useStrict;

		data.firstArg = firstArg;
		data.hasDontEnumBug = hasDontEnumBug;
		data.isKeysFast = isKeysFast;
		data.noArgsEnum = noArgsEnum;
		data.shadowed = shadowed;
		data.useHas = data.useHas !== false;
		data.useStrict = useStrict == null ? isStrictFast : useStrict;

		if (data.noCharByIndex == null) {
			data.noCharByIndex = noCharByIndex;
		}
		if (!data.exit) {
			data.exit = 'if (!' + firstArg + ') return result';
		}
		if (firstArg != 'collection' || !data.arrayBranch.inLoop) {
			data.arrayBranch = null;
		}
		// create the function factory
		var factory = Function(
				'arrayLikeClasses, ArrayProto, bind, compareAscending, concat, forIn, ' +
				'hasOwnProperty, identity, indexOf, isArguments, isArray, isFunction, ' +
				'isPlainObject, iteratorBind, objectClass, objectTypes, nativeKeys, ' +
				'propertyIsEnumerable, slice, stringClass, toString',
			'var callee = function(' + args + ') {\n' + iteratorTemplate(data) + '\n};\n' +
			'return callee'
		);
		// return the compiled function
		return factory(
			arrayLikeClasses, ArrayProto, bind, compareAscending, concat, forIn,
			hasOwnProperty, identity, indexOf, isArguments, isArray, isFunction,
			isPlainObject, iteratorBind, objectClass, objectTypes, nativeKeys,
			propertyIsEnumerable, slice, stringClass, toString
		);
	}

	/**
	 * Used by `sortBy` to compare transformed `collection` values, stable sorting
	 * them in ascending order.
	 *
	 * @private
	 * @param {Object} a The object to compare to `b`.
	 * @param {Object} b The object to compare to `a`.
	 * @returns {Number} Returns the sort order indicator of `1` or `-1`.
	 */
	function compareAscending(a, b) {
		var ai = a.index,
				bi = b.index;

		a = a.criteria;
		b = b.criteria;

		if (a === undefined) {
			return 1;
		}
		if (b === undefined) {
			return -1;
		}
		// ensure a stable sort in V8 and other engines
		// http://code.google.com/p/v8/issues/detail?id=90
		return a < b ? -1 : a > b ? 1 : ai < bi ? -1 : 1;
	}

	/**
	 * Used by `template` to replace tokens with their corresponding code snippets.
	 *
	 * @private
	 * @param {String} match The matched token.
	 * @param {String} index The `tokenized` index of the code snippet.
	 * @returns {String} Returns the code snippet.
	 */
	function detokenize(match, index) {
		return tokenized[index];
	}

	/**
	 * Used by `template` to escape characters for inclusion in compiled
	 * string literals.
	 *
	 * @private
	 * @param {String} match The matched character to escape.
	 * @returns {String} Returns the escaped character.
	 */
	function escapeStringChar(match) {
		return '\\' + stringEscapes[match];
	}

	/**
	 * Used by `escape` to convert characters to HTML entities.
	 *
	 * @private
	 * @param {String} match The matched character to escape.
	 * @returns {String} Returns the escaped character.
	 */
	function escapeHtmlChar(match) {
		return htmlEscapes[match];
	}

	/**
	 * Creates a new function that, when called, invokes `func` with the `this`
	 * binding of `thisArg` and the arguments (value, index, object).
	 *
	 * @private
	 * @param {Function} func The function to bind.
	 * @param {Mixed} [thisArg] The `this` binding of `func`.
	 * @returns {Function} Returns the new bound function.
	 */
	function iteratorBind(func, thisArg) {
		return function(value, index, object) {
			return func.call(thisArg, value, index, object);
		};
	}

	/**
	 * A no-operation function.
	 *
	 * @private
	 */
	function noop() {
		// no operation performed
	}

	/**
	 * Used by `template` to replace "escape" template delimiters with tokens.
	 *
	 * @private
	 * @param {String} match The matched template delimiter.
	 * @param {String} value The delimiter value.
	 * @returns {String} Returns a token.
	 */
	function tokenizeEscape(match, value) {
		if (match && reComplexDelimiter.test(value)) {
			return '<e%-' + value + '%>';
		}
		var index = tokenized.length;
		tokenized[index] = "' +\n__e(" + value + ") +\n'";
		return token + index;
	}

	/**
	 * Used by `template` to replace "evaluate" template delimiters, or complex
	 * "escape" and "interpolate" delimiters, with tokens.
	 *
	 * @private
	 * @param {String} match The matched template delimiter.
	 * @param {String} escapeValue The complex "escape" delimiter value.
	 * @param {String} interpolateValue The complex "interpolate" delimiter value.
	 * @param {String} [evaluateValue] The "evaluate" delimiter value.
	 * @returns {String} Returns a token.
	 */
	function tokenizeEvaluate(match, escapeValue, interpolateValue, evaluateValue) {
		if (evaluateValue) {
			var index = tokenized.length;
			tokenized[index] = "';\n" + evaluateValue + ";\n__p += '";
			return token + index;
		}
		return escapeValue
			? tokenizeEscape(null, escapeValue)
			: tokenizeInterpolate(null, interpolateValue);
	}

	/**
	 * Used by `template` to replace "interpolate" template delimiters with tokens.
	 *
	 * @private
	 * @param {String} match The matched template delimiter.
	 * @param {String} value The delimiter value.
	 * @returns {String} Returns a token.
	 */
	function tokenizeInterpolate(match, value) {
		if (match && reComplexDelimiter.test(value)) {
			return '<e%=' + value + '%>';
		}
		var index = tokenized.length;
		tokenized[index] = "' +\n((__t = (" + value + ")) == null ? '' : __t) +\n'";
		return token + index;
	}

	/**
	 * Used by `unescape` to convert HTML entities to characters.
	 *
	 * @private
	 * @param {String} match The matched character to unescape.
	 * @returns {String} Returns the unescaped character.
	 */
	function unescapeHtmlChar(match) {
		return htmlUnescapes[match];
	}

	/*--------------------------------------------------------------------------*/

	/**
	 * Checks if `value` is an `arguments` object.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is an `arguments` object, else `false`.
	 * @example
	 *
	 * (function() { return _.isArguments(arguments); })(1, 2, 3);
	 * // => true
	 *
	 * _.isArguments([1, 2, 3]);
	 * // => false
	 */
	function isArguments(value) {
		return toString.call(value) == argsClass;
	}
	// fallback for browsers that can't detect `arguments` objects by [[Class]]
	if (noArgsClass) {
		isArguments = function(value) {
			return !!(value && hasOwnProperty.call(value, 'callee'));
		};
	}

	/**
	 * Checks if `value` is an array.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is an array, else `false`.
	 * @example
	 *
	 * (function() { return _.isArray(arguments); })();
	 * // => false
	 *
	 * _.isArray([1, 2, 3]);
	 * // => true
	 */
	var isArray = nativeIsArray || function(value) {
		return toString.call(value) == arrayClass;
	};

	/**
	 * Checks if `value` is a function.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is a function, else `false`.
	 * @example
	 *
	 * _.isFunction(''.concat);
	 * // => true
	 */
	function isFunction(value) {
		return typeof value == 'function';
	}
	// fallback for older versions of Chrome and Safari
	if (isFunction(/x/)) {
		isFunction = function(value) {
			return toString.call(value) == funcClass;
		};
	}

	/**
	 * Checks if a given `value` is an object created by the `Object` constructor
	 * assuming objects created by the `Object` constructor have no inherited
	 * enumerable properties and that there are no `Object.prototype` extensions.
	 *
	 * @private
	 * @param {Mixed} value The value to check.
	 * @param {Boolean} [skipArgsCheck=false] Internally used to skip checks for
	 *  `arguments` objects.
	 * @returns {Boolean} Returns `true` if the `value` is a plain `Object` object,
	 *  else `false`.
	 */
	function isPlainObject(value, skipArgsCheck) {
		return value
			? value == ObjectProto || (value.__proto__ == ObjectProto && (skipArgsCheck || !isArguments(value)))
			: false;
	}
	// fallback for IE
	if (!isPlainObject(objectTypes)) {
		isPlainObject = function(value, skipArgsCheck) {
			// avoid non-objects and false positives for `arguments` objects
			var result = false;
			if (!(value && typeof value == 'object') || (!skipArgsCheck && isArguments(value))) {
				return result;
			}
			// IE < 9 presents DOM nodes as `Object` objects except they have `toString`
			// methods that are `typeof` "string" and still can coerce nodes to strings.
			// Also check that the constructor is `Object` (i.e. `Object instanceof Object`)
			var ctor = value.constructor;
			if ((!noNodeClass || !(typeof value.toString != 'function' && typeof (value + '') == 'string')) &&
					(!isFunction(ctor) || ctor instanceof ctor)) {
				// IE < 9 iterates inherited properties before own properties. If the first
				// iterated property is an object's own property then there are no inherited
				// enumerable properties.
				if (iteratesOwnLast) {
					forIn(value, function(objValue, objKey) {
						result = !hasOwnProperty.call(value, objKey);
						return false;
					});
					return result === false;
				}
				// In most environments an object's own properties are iterated before
				// its inherited properties. If the last iterated property is an object's
				// own property then there are no inherited enumerable properties.
				forIn(value, function(objValue, objKey) {
					result = objKey;
				});
				return result === false || hasOwnProperty.call(value, result);
			}
			return result;
		};
	}

	/**
	 * A shim implementation of `Object.keys` that produces an array of the given
	 * object's own enumerable property names.
	 *
	 * @private
	 * @param {Object} object The object to inspect.
	 * @returns {Array} Returns a new array of property names.
	 */
	var shimKeys = createIterator({
		'args': 'object',
		'init': '[]',
		'inLoop': 'result.push(index)'
	});

	/*--------------------------------------------------------------------------*/

	/**
	 * Creates a clone of `value`. If `deep` is `true`, all nested objects will
	 * also be cloned otherwise they will be assigned by reference. If a value has
	 * a `clone` method it will be used to perform the clone. Functions, DOM nodes,
	 * `arguments` objects, and objects created by constructors other than `Object`
	 * are **not** cloned unless they have a custom `clone` method.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to clone.
	 * @param {Boolean} deep A flag to indicate a deep clone.
	 * @param {Object} [guard] Internally used to allow this method to work with
	 *  others like `_.map` without using their callback `index` argument for `deep`.
	 * @param {Array} [stack=[]] Internally used to keep track of traversed objects
	 *  to avoid circular references.
	 * @param {Object} thorough Internally used to indicate whether or not to perform
	 *  a more thorough clone of non-object values.
	 * @returns {Mixed} Returns the cloned `value`.
	 * @example
	 *
	 * var stooges = [
	 *   { 'name': 'moe', 'age': 40 },
	 *   { 'name': 'larry', 'age': 50 },
	 *   { 'name': 'curly', 'age': 60 }
	 * ];
	 *
	 * _.clone({ 'name': 'moe' });
	 * // => { 'name': 'moe' }
	 *
	 * var shallow = _.clone(stooges);
	 * shallow[0] === stooges[0];
	 * // => true
	 *
	 * var deep = _.clone(stooges, true);
	 * shallow[0] === stooges[0];
	 * // => false
	 */
	function clone(value, deep, guard, stack, thorough) {
		if (value == null) {
			return value;
		}
		if (guard) {
			deep = false;
		}
		// avoid slower checks on primitives
		thorough || (thorough = { 'value': null });
		if (thorough.value == null) {
			// primitives passed from iframes use the primary document's native prototypes
			thorough.value = !!(BoolProto.clone || NumberProto.clone || StringProto.clone);
		}
		// use custom `clone` method if available
		var isObj = objectTypes[typeof value];
		if ((isObj || thorough.value) && value.clone && isFunction(value.clone)) {
			thorough.value = null;
			return value.clone(deep);
		}
		// inspect [[Class]]
		if (isObj) {
			// don't clone `arguments` objects, functions, or non-object Objects
			var className = toString.call(value);
			if (!cloneableClasses[className] || (noArgsClass && isArguments(value))) {
				return value;
			}
			var isArr = className == arrayClass;
			isObj = isArr || (className == objectClass ? isPlainObject(value, true) : isObj);
		}
		// shallow clone
		if (!isObj || !deep) {
			// don't clone functions
			return isObj
				? (isArr ? slice.call(value) : extend({}, value))
				: value;
		}

		var ctor = value.constructor;
		switch (className) {
			case boolClass:
				return new ctor(value == true);

			case dateClass:
				return new ctor(+value);

			case numberClass:
			case stringClass:
				return new ctor(value);

			case regexpClass:
				return ctor(value.source, reFlags.exec(value));
		}

		// check for circular references and return corresponding clone
		stack || (stack = []);
		var length = stack.length;
		while (length--) {
			if (stack[length].source == value) {
				return stack[length].value;
			}
		}

		// init cloned object
		length = value.length;
		var result = isArr ? ctor(length) : {};

		// add current clone and original source value to the stack of traversed objects
		stack.push({ 'value': result, 'source': value });

		// recursively populate clone (susceptible to call stack limits)
		if (isArr) {
			var index = -1;
			while (++index < length) {
				result[index] = clone(value[index], deep, null, stack, thorough);
			}
		} else {
			forOwn(value, function(objValue, key) {
				result[key] = clone(objValue, deep, null, stack, thorough);
			});
		}
		return result;
	}

	/**
	 * Assigns enumerable properties of the default object(s) to the `destination`
	 * object for all `destination` properties that resolve to `null`/`undefined`.
	 * Once a property is set, additional defaults of the same property will be
	 * ignored.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Object} object The destination object.
	 * @param {Object} [default1, default2, ...] The default objects.
	 * @returns {Object} Returns the destination object.
	 * @example
	 *
	 * var iceCream = { 'flavor': 'chocolate' };
	 * _.defaults(iceCream, { 'flavor': 'vanilla', 'sprinkles': 'rainbow' });
	 * // => { 'flavor': 'chocolate', 'sprinkles': 'rainbow' }
	 */
	var defaults = createIterator(extendIteratorOptions, {
		'inLoop': 'if (result[index] == null) ' + extendIteratorOptions.inLoop
	});

	/**
	 * Creates a shallow clone of `object` excluding the specified properties.
	 * Property names may be specified as individual arguments or as arrays of
	 * property names. If `callback` is passed, it will be executed for each property
	 * in the `object`, dropping the properties `callback` returns truthy for. The
	 * `callback` is bound to `thisArg` and invoked with 3 arguments; (value, key, object).
	 *
	 * @static
	 * @memberOf _
	 * @alias omit
	 * @category Objects
	 * @param {Object} object The source object.
	 * @param {Function|String} callback|[prop1, prop2, ...] The properties to drop
	 *  or the function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Object} Returns an object without the dropped properties.
	 * @example
	 *
	 * _.drop({ 'name': 'moe', 'age': 40, 'userid': 'moe1' }, 'userid');
	 * // => { 'name': 'moe', 'age': 40 }
	 *
	 * _.drop({ 'name': 'moe', '_hint': 'knucklehead', '_seed': '96c4eb' }, function(value, key) {
	 *   return key.charAt(0) == '_';
	 * });
	 * // => { 'name': 'moe' }
	 */
	var drop = createIterator(dropIteratorOptions);

	/**
	 * Assigns enumerable properties of the source object(s) to the `destination`
	 * object. Subsequent sources will overwrite propery assignments of previous
	 * sources.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Object} object The destination object.
	 * @param {Object} [source1, source2, ...] The source objects.
	 * @returns {Object} Returns the destination object.
	 * @example
	 *
	 * _.extend({ 'name': 'moe' }, { 'age': 40 });
	 * // => { 'name': 'moe', 'age': 40 }
	 */
	var extend = createIterator(extendIteratorOptions);

	/**
	 * Iterates over `object`'s own and inherited enumerable properties, executing
	 * the `callback` for each property. The `callback` is bound to `thisArg` and
	 * invoked with 3 arguments; (value, key, object). Callbacks may exit iteration
	 * early by explicitly returning `false`.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Object} object The object to iterate over.
	 * @param {Function} callback The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Object} Returns `object`.
	 * @example
	 *
	 * function Dog(name) {
	 *   this.name = name;
	 * }
	 *
	 * Dog.prototype.bark = function() {
	 *   alert('Woof, woof!');
	 * };
	 *
	 * _.forIn(new Dog('Dagny'), function(value, key) {
	 *   alert(key);
	 * });
	 * // => alerts 'name' and 'bark' (order is not guaranteed)
	 */
	var forIn = createIterator(baseIteratorOptions, forEachIteratorOptions, forOwnIteratorOptions, {
		'useHas': false
	});

	/**
	 * Iterates over `object`'s own enumerable properties, executing the `callback`
	 * for each property. The `callback` is bound to `thisArg` and invoked with 3
	 * arguments; (value, key, object). Callbacks may exit iteration early by
	 * explicitly returning `false`.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Object} object The object to iterate over.
	 * @param {Function} callback The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Object} Returns `object`.
	 * @example
	 *
	 * _.forOwn({ '0': 'zero', '1': 'one', 'length': 2 }, function(num, key) {
	 *   alert(key);
	 * });
	 * // => alerts '0', '1', and 'length' (order is not guaranteed)
	 */
	var forOwn = createIterator(baseIteratorOptions, forEachIteratorOptions, forOwnIteratorOptions);

	/**
	 * Creates a sorted array of all enumerable properties, own and inherited,
	 * of `object` that have function values.
	 *
	 * @static
	 * @memberOf _
	 * @alias methods
	 * @category Objects
	 * @param {Object} object The object to inspect.
	 * @returns {Array} Returns a new array of property names that have function values.
	 * @example
	 *
	 * _.functions(_);
	 * // => ['all', 'any', 'bind', 'bindAll', 'clone', 'compact', 'compose', ...]
	 */
	var functions = createIterator({
		'useHas': false,
		'args': 'object',
		'init': '[]',
		'inLoop': 'if (isFunction(value)) result.push(index)',
		'bottom': 'result.sort()'
	});

	/**
	 * Checks if the specified object `property` exists and is a direct property,
	 * instead of an inherited property.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Object} object The object to check.
	 * @param {String} property The property to check for.
	 * @returns {Boolean} Returns `true` if key is a direct property, else `false`.
	 * @example
	 *
	 * _.has({ 'a': 1, 'b': 2, 'c': 3 }, 'b');
	 * // => true
	 */
	function has(object, property) {
		return object ? hasOwnProperty.call(object, property) : false;
	}

	/**
	 * Checks if `value` is a boolean (`true` or `false`) value.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is a boolean value, else `false`.
	 * @example
	 *
	 * _.isBoolean(null);
	 * // => false
	 */
	function isBoolean(value) {
		return value === true || value === false || toString.call(value) == boolClass;
	}

	/**
	 * Checks if `value` is a date.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is a date, else `false`.
	 * @example
	 *
	 * _.isDate(new Date);
	 * // => true
	 */
	function isDate(value) {
		return toString.call(value) == dateClass;
	}

	/**
	 * Checks if `value` is a DOM element.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is a DOM element, else `false`.
	 * @example
	 *
	 * _.isElement(document.body);
	 * // => true
	 */
	function isElement(value) {
		return value ? value.nodeType === 1 : false;
	}

	/**
	 * Checks if `value` is empty. Arrays, strings, or `arguments` objects with a
	 * length of `0` and objects with no own enumerable properties are considered
	 * "empty".
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Array|Object|String} value The value to inspect.
	 * @returns {Boolean} Returns `true` if the `value` is empty, else `false`.
	 * @example
	 *
	 * _.isEmpty([1, 2, 3]);
	 * // => false
	 *
	 * _.isEmpty({});
	 * // => true
	 *
	 * _.isEmpty('');
	 * // => true
	 */
	var isEmpty = createIterator({
		'args': 'value',
		'init': 'true',
		'top':
			'var className = toString.call(value),\n' +
			'    length = value.length;\n' +
			'if (arrayLikeClasses[className]' +
			(noArgsClass ? ' || isArguments(value)' : '') + ' ||\n' +
			'  (className == objectClass && length > -1 && length === length >>> 0 &&\n' +
			'  isFunction(value.splice))' +
			') return !length',
		'inLoop': {
			'object': 'return false'
		}
	});

	/**
	 * Performs a deep comparison between two values to determine if they are
	 * equivalent to each other. If a value has an `isEqual` method it will be
	 * used to perform the comparison.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} a The value to compare.
	 * @param {Mixed} b The other value to compare.
	 * @param {Array} [stack=[]] Internally used to keep track of traversed objects
	 *  to avoid circular references.
	 * @param {Object} thorough Internally used to indicate whether or not to perform
	 *  a more thorough comparison of non-object values.
	 * @returns {Boolean} Returns `true` if the values are equvalent, else `false`.
	 * @example
	 *
	 * var moe = { 'name': 'moe', 'luckyNumbers': [13, 27, 34] };
	 * var clone = { 'name': 'moe', 'luckyNumbers': [13, 27, 34] };
	 *
	 * moe == clone;
	 * // => false
	 *
	 * _.isEqual(moe, clone);
	 * // => true
	 */
	function isEqual(a, b, stack, thorough) {
		// a strict comparison is necessary because `null == undefined`
		if (a == null || b == null) {
			return a === b;
		}
		// avoid slower checks on non-objects
		thorough || (thorough = { 'value': null });
		if (thorough.value == null) {
			// primitives passed from iframes use the primary document's native prototypes
			thorough.value = !!(BoolProto.isEqual || NumberProto.isEqual || StringProto.isEqual);
		}
		if (objectTypes[typeof a] || objectTypes[typeof b] || thorough.value) {
			// unwrap any LoDash wrapped values
			if (a._chain) {
				a = a._wrapped;
			}
			if (b._chain) {
				b = b._wrapped;
			}
			// use custom `isEqual` method if available
			if (a.isEqual && isFunction(a.isEqual)) {
				thorough.value = null;
				return a.isEqual(b);
			}
			if (b.isEqual && isFunction(b.isEqual)) {
				thorough.value = null;
				return b.isEqual(a);
			}
		}
		// exit early for identical values
		if (a === b) {
			// treat `+0` vs. `-0` as not equal
			return a !== 0 || (1 / a == 1 / b);
		}
		// compare [[Class]] names
		var className = toString.call(a);
		if (className != toString.call(b)) {
			return false;
		}
		switch (className) {
			case boolClass:
			case dateClass:
				// coerce dates and booleans to numbers, dates to milliseconds and booleans
				// to `1` or `0`, treating invalid dates coerced to `NaN` as not equal
				return +a == +b;

			case numberClass:
				// treat `NaN` vs. `NaN` as equal
				return a != +a
					? b != +b
					// but treat `+0` vs. `-0` as not equal
					: (a == 0 ? (1 / a == 1 / b) : a == +b);

			case regexpClass:
			case stringClass:
				// coerce regexes to strings (http://es5.github.com/#x15.10.6.4)
				// treat string primitives and their corresponding object instances as equal
				return a == b + '';
		}
		// exit early, in older browsers, if `a` is array-like but not `b`
		var isArr = arrayLikeClasses[className];
		if (noArgsClass && !isArr && (isArr = isArguments(a)) && !isArguments(b)) {
			return false;
		}
		// exit for functions and DOM nodes
		if (!isArr && (className != objectClass || (noNodeClass && (
				(typeof a.toString != 'function' && typeof (a + '') == 'string') ||
				(typeof b.toString != 'function' && typeof (b + '') == 'string'))))) {
			return false;
		}

		// assume cyclic structures are equal
		// the algorithm for detecting cyclic structures is adapted from ES 5.1
		// section 15.12.3, abstract operation `JO` (http://es5.github.com/#x15.12.3)
		stack || (stack = []);
		var length = stack.length;
		while (length--) {
			if (stack[length] == a) {
				return true;
			}
		}

		var index = -1,
				result = true,
				size = 0;

		// add `a` to the stack of traversed objects
		stack.push(a);

		// recursively compare objects and arrays (susceptible to call stack limits)
		if (isArr) {
			// compare lengths to determine if a deep comparison is necessary
			size = a.length;
			result = size == b.length;

			if (result) {
				// deep compare the contents, ignoring non-numeric properties
				while (size--) {
					if (!(result = isEqual(a[size], b[size], stack, thorough))) {
						break;
					}
				}
			}
			return result;
		}

		var ctorA = a.constructor,
				ctorB = b.constructor;

		// non `Object` object instances with different constructors are not equal
		if (ctorA != ctorB && !(
					isFunction(ctorA) && ctorA instanceof ctorA &&
					isFunction(ctorB) && ctorB instanceof ctorB
				)) {
			return false;
		}
		// deep compare objects
		for (var prop in a) {
			if (hasOwnProperty.call(a, prop)) {
				// count the number of properties.
				size++;
				// deep compare each property value.
				if (!(hasOwnProperty.call(b, prop) && isEqual(a[prop], b[prop], stack, thorough))) {
					return false;
				}
			}
		}
		// ensure both objects have the same number of properties
		for (prop in b) {
			// The JS engine in Adobe products, like InDesign, has a bug that causes
			// `!size--` to throw an error so it must be wrapped in parentheses.
			// https://github.com/documentcloud/underscore/issues/355
			if (hasOwnProperty.call(b, prop) && !(size--)) {
				// `size` will be `-1` if `b` has more properties than `a`
				return false;
			}
		}
		// handle JScript [[DontEnum]] bug
		if (hasDontEnumBug) {
			while (++index < 7) {
				prop = shadowed[index];
				if (hasOwnProperty.call(a, prop) &&
						!(hasOwnProperty.call(b, prop) && isEqual(a[prop], b[prop], stack, thorough))) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks if `value` is a finite number.
	 *
	 * Note: This is not the same as native `isFinite`, which will return true for
	 * booleans and other values. See http://es5.github.com/#x15.1.2.5.
	 *
	 * @deprecated
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is a finite number, else `false`.
	 * @example
	 *
	 * _.isFinite(-101);
	 * // => true
	 *
	 * _.isFinite('10');
	 * // => false
	 *
	 * _.isFinite(Infinity);
	 * // => false
	 */
	function isFinite(value) {
		return nativeIsFinite(value) && toString.call(value) == numberClass;
	}

	/**
	 * Checks if `value` is the language type of Object.
	 * (e.g. arrays, functions, objects, regexes, `new Number(0)`, and `new String('')`)
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is an object, else `false`.
	 * @example
	 *
	 * _.isObject({});
	 * // => true
	 *
	 * _.isObject(1);
	 * // => false
	 */
	function isObject(value) {
		// check if the value is the ECMAScript language type of Object
		// http://es5.github.com/#x8
		// and avoid a V8 bug
		// http://code.google.com/p/v8/issues/detail?id=2291
		return value ? objectTypes[typeof value] : false;
	}

	/**
	 * Checks if `value` is `NaN`.
	 *
	 * Note: This is not the same as native `isNaN`, which will return true for
	 * `undefined` and other values. See http://es5.github.com/#x15.1.2.4.
	 *
	 * @deprecated
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is `NaN`, else `false`.
	 * @example
	 *
	 * _.isNaN(NaN);
	 * // => true
	 *
	 * _.isNaN(new Number(NaN));
	 * // => true
	 *
	 * isNaN(undefined);
	 * // => true
	 *
	 * _.isNaN(undefined);
	 * // => false
	 */
	function isNaN(value) {
		// `NaN` as a primitive is the only value that is not equal to itself
		// (perform the [[Class]] check first to avoid errors with some host objects in IE)
		return toString.call(value) == numberClass && value != +value
	}

	/**
	 * Checks if `value` is `null`.
	 *
	 * @deprecated
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is `null`, else `false`.
	 * @example
	 *
	 * _.isNull(null);
	 * // => true
	 *
	 * _.isNull(undefined);
	 * // => false
	 */
	function isNull(value) {
		return value === null;
	}

	/**
	 * Checks if `value` is a number.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is a number, else `false`.
	 * @example
	 *
	 * _.isNumber(8.4 * 5;
	 * // => true
	 */
	function isNumber(value) {
		return toString.call(value) == numberClass;
	}

	/**
	 * Checks if `value` is a regular expression.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is a regular expression, else `false`.
	 * @example
	 *
	 * _.isRegExp(/moe/);
	 * // => true
	 */
	function isRegExp(value) {
		return toString.call(value) == regexpClass;
	}

	/**
	 * Checks if `value` is a string.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is a string, else `false`.
	 * @example
	 *
	 * _.isString('moe');
	 * // => true
	 */
	function isString(value) {
		return toString.call(value) == stringClass;
	}

	/**
	 * Checks if `value` is `undefined`.
	 *
	 * @deprecated
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Mixed} value The value to check.
	 * @returns {Boolean} Returns `true` if the `value` is `undefined`, else `false`.
	 * @example
	 *
	 * _.isUndefined(void 0);
	 * // => true
	 */
	function isUndefined(value) {
		return value === undefined;
	}

	/**
	 * Creates an array composed of the own enumerable property names of `object`.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Object} object The object to inspect.
	 * @returns {Array} Returns a new array of property names.
	 * @example
	 *
	 * _.keys({ 'one': 1, 'two': 2, 'three': 3 });
	 * // => ['one', 'two', 'three'] (order is not guaranteed)
	 */
	var keys = !nativeKeys ? shimKeys : function(object) {
		var type = typeof object;

		// avoid iterating over the `prototype` property
		if (type == 'function' && propertyIsEnumerable.call(object, 'prototype')) {
			return shimKeys(object);
		}
		return object && objectTypes[type]
			? nativeKeys(object)
			: [];
	};

	/**
	 * Merges enumerable properties of the source object(s) into the `destination`
	 * object. Subsequent sources will overwrite propery assignments of previous
	 * sources.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Object} object The destination object.
	 * @param {Object} [source1, source2, ...] The source objects.
	 * @param {Object} [indicator] Internally used to indicate that the `stack`
	 *  argument is an array of traversed objects instead of another source object.
	 * @param {Array} [stack=[]] Internally used to keep track of traversed objects
	 *  to avoid circular references.
	 * @returns {Object} Returns the destination object.
	 * @example
	 *
	 * var stooges = [
	 *   { 'name': 'moe' },
	 *   { 'name': 'larry' }
	 * ];
	 *
	 * var ages = [
	 *   { 'age': 40 },
	 *   { 'age': 50 }
	 * ];
	 *
	 * _.merge(stooges, ages);
	 * // => [{ 'name': 'moe', 'age': 40 }, { 'name': 'larry', 'age': 50 }]
	 */
	var merge = createIterator(extendIteratorOptions, {
		'args': 'object, source, indicator, stack',
		'top':
			'var destValue, found, isArr, stackLength, recursive = indicator == isPlainObject;\n' +
			'if (!recursive) stack = [];\n' +
			'for (var argsIndex = 1, argsLength = recursive ? 2 : arguments.length; argsIndex < argsLength; argsIndex++) {\n' +
			'  if (iteratee = arguments[argsIndex]) {',
		'inLoop':
			'if (value && ((isArr = isArray(value)) || isPlainObject(value))) {\n' +
			'  found = false; stackLength = stack.length;\n' +
			'  while (stackLength--) {\n' +
			'    if (found = stack[stackLength].source == value) break\n' +
			'  }\n' +
			'  if (found) {\n' +
			'    result[index] = stack[stackLength].value\n' +
			'  } else {\n' +
			'    destValue = (destValue = result[index]) && isArr\n' +
			'      ? (isArray(destValue) ? destValue : [])\n' +
			'      : (isPlainObject(destValue) ? destValue : {});\n' +
			'    stack.push({ value: destValue, source: value });\n' +
			'    result[index] = callee(destValue, value, isPlainObject, stack)\n' +
			'  }\n' +
			'} else if (value != null) {\n' +
			'  result[index] = value\n' +
			'}'
	});

	/**
	 * Creates a shallow clone of `object` composed of the specified properties.
	 * Property names may be specified as individual arguments or as arrays of
	 * property names. If `callback` is passed, it will be executed for each property
	 * in the `object`, picking the properties `callback` returns truthy for. The
	 * `callback` is bound to `thisArg` and invoked with 3 arguments; (value, key, object).
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Object} object The source object.
	 * @param {Function|String} callback|[prop1, prop2, ...] The properties to pick
	 *  or the function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Object} Returns an object composed of the picked properties.
	 * @example
	 *
	 * _.pick({ 'name': 'moe', 'age': 40, 'userid': 'moe1' }, 'name', 'age');
	 * // => { 'name': 'moe', 'age': 40 }
	 *
	 * _.pick({ 'name': 'moe', '_hint': 'knucklehead', '_seed': '96c4eb' }, function(value, key) {
	 *   return key.charAt(0) != '_';
	 * });
	 * // => { 'name': 'moe' }
	 */
	var pick = createIterator(dropIteratorOptions, {
		'top':
			'if (typeof callback != \'function\') {\n' +
			'  var prop,\n' +
			'      props = concat.apply(ArrayProto, arguments),\n' +
			'      length = props.length;\n' +
			'  for (index = 1; index < length; index++) {\n' +
			'    prop = props[index];\n' +
			'    if (prop in object) result[prop] = object[prop]\n' +
			'  }\n' +
			'} else {\n' +
			'  if (thisArg) callback = iteratorBind(callback, thisArg)',
		'inLoop':
			'if (callback(value, index, object)) result[index] = value',
		'bottom': '}'
	});

	/**
	 * Gets the size of `value` by returning `value.length` if `value` is an
	 * array, string, or `arguments` object. If `value` is an object, size is
	 * determined by returning the number of own enumerable properties it has.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Array|Object|String} value The value to inspect.
	 * @returns {Number} Returns `value.length` or number of own enumerable properties.
	 * @example
	 *
	 * _.size([1, 2]);
	 * // => 2
	 *
	 * _.size({ 'one': 1, 'two': 2, 'three': 3 });
	 * // => 3
	 *
	 * _.size('curly');
	 * // => 5
	 */
	function size(value) {
		if (!value) {
			return 0;
		}
		var className = toString.call(value),
				length = value.length;

		// return `value.length` for `arguments` objects, arrays, strings, and DOM
		// query collections of libraries like jQuery and MooTools
		// http://code.google.com/p/fbug/source/browse/branches/firebug1.9/content/firebug/chrome/reps.js?r=12614#653
		// http://trac.webkit.org/browser/trunk/Source/WebCore/inspector/InjectedScriptSource.js?rev=125186#L609
		if (arrayLikeClasses[className] || (noArgsClass && isArguments(value)) ||
				(className == objectClass && length > -1 && length === length >>> 0 && isFunction(value.splice))) {
			return length;
		}
		return keys(value).length;
	}

	/**
	 * Creates an array composed of the own enumerable property values of `object`.
	 *
	 * @static
	 * @memberOf _
	 * @category Objects
	 * @param {Object} object The object to inspect.
	 * @returns {Array} Returns a new array of property values.
	 * @example
	 *
	 * _.values({ 'one': 1, 'two': 2, 'three': 3 });
	 * // => [1, 2, 3]
	 */
	var values = createIterator({
		'args': 'object',
		'init': '[]',
		'inLoop': 'result.push(value)'
	});

	/*--------------------------------------------------------------------------*/

	/**
	 * Checks if a given `target` element is present in a `collection` using strict
	 * equality for comparisons, i.e. `===`.
	 *
	 * @static
	 * @memberOf _
	 * @alias include
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Mixed} target The value to check for.
	 * @returns {Boolean} Returns `true` if the `target` element is found, else `false`.
	 * @example
	 *
	 * _.contains([1, 2, 3], 3);
	 * // => true
	 *
	 * _.contains({ 'name': 'moe', 'age': 40 }, 'moe');
	 * // => true
	 *
	 * _.contains('curly', 'ur');
	 * // => true
	 */
	var contains = createIterator({
		'args': 'collection, target',
		'init': 'false',
		'noCharByIndex': false,
		'beforeLoop': {
			'array': 'if (toString.call(collection) == stringClass) return collection.indexOf(target) > -1'
		},
		'inLoop': 'if (value === target) return true'
	});

	/**
	 * Creates an object composed of keys returned from running each element of
	 * `collection` through a `callback`. The corresponding value of each key is
	 * the number of times the key was returned by `callback`. The `callback` is
	 * bound to `thisArg` and invoked with 3 arguments; (value, index|key, collection).
	 * The `callback` argument may also be the name of a property to count by (e.g. 'length').
	 *
	 * @static
	 * @memberOf _
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Function|String} callback|property The function called per iteration
	 *  or property name to count by.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Object} Returns the composed aggregate object.
	 * @example
	 *
	 * _.countBy([4.3, 6.1, 6.4], function(num) { return Math.floor(num); });
	 * // => { '4': 1, '6': 2 }
	 *
	 * _.countBy([4.3, 6.1, 6.4], function(num) { return this.floor(num); }, Math);
	 * // => { '4': 1, '6': 2 }
	 *
	 * _.countBy(['one', 'two', 'three'], 'length');
	 * // => { '3': 2, '5': 1 }
	 */
	var countBy = createIterator(baseIteratorOptions, countByIteratorOptions);

	/**
	 * Checks if the `callback` returns a truthy value for **all** elements of a
	 * `collection`. The `callback` is bound to `thisArg` and invoked with 3
	 * arguments; (value, index|key, collection).
	 *
	 * @static
	 * @memberOf _
	 * @alias all
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Function} [callback=identity] The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Boolean} Returns `true` if all elements pass the callback check, else `false`.
	 * @example
	 *
	 * _.every([true, 1, null, 'yes'], Boolean);
	 * // => false
	 */
	var every = createIterator(baseIteratorOptions, everyIteratorOptions);

	/**
	 * Examines each element in a `collection`, returning an array of all elements
	 * the `callback` returns truthy for. The `callback` is bound to `thisArg` and
	 * invoked with 3 arguments; (value, index|key, collection).
	 *
	 * @static
	 * @memberOf _
	 * @alias select
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Function} [callback=identity] The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Array} Returns a new array of elements that passed callback check.
	 * @example
	 *
	 * var evens = _.filter([1, 2, 3, 4, 5, 6], function(num) { return num % 2 == 0; });
	 * // => [2, 4, 6]
	 */
	var filter = createIterator(baseIteratorOptions, filterIteratorOptions);

	/**
	 * Examines each element in a `collection`, returning the first one the `callback`
	 * returns truthy for. The function returns as soon as it finds an acceptable
	 * element, and does not iterate over the entire `collection`. The `callback` is
	 * bound to `thisArg` and invoked with 3 arguments; (value, index|key, collection).
	 *
	 * @static
	 * @memberOf _
	 * @alias detect
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Function} callback The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Mixed} Returns the element that passed the callback check, else `undefined`.
	 * @example
	 *
	 * var even = _.find([1, 2, 3, 4, 5, 6], function(num) { return num % 2 == 0; });
	 * // => 2
	 */
	var find = createIterator(baseIteratorOptions, forEachIteratorOptions, {
		'init': '',
		'inLoop': 'if (callback(value, index, collection)) return value'
	});

	/**
	 * Iterates over a `collection`, executing the `callback` for each element in
	 * the `collection`. The `callback` is bound to `thisArg` and invoked with 3
	 * arguments; (value, index|key, collection). Callbacks may exit iteration
	 * early by explicitly returning `false`.
	 *
	 * @static
	 * @memberOf _
	 * @alias each
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Function} callback The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Array|Object} Returns `collection`.
	 * @example
	 *
	 * _([1, 2, 3]).forEach(alert).join(',');
	 * // => alerts each number and returns '1,2,3'
	 *
	 * _.forEach({ 'one': 1, 'two': 2, 'three': 3 }, alert);
	 * // => alerts each number (order is not guaranteed)
	 */
	var forEach = createIterator(baseIteratorOptions, forEachIteratorOptions);

	/**
	 * Creates an object composed of keys returned from running each element of
	 * `collection` through a `callback`. The corresponding value of each key is an
	 * array of elements passed to `callback` that returned the key. The `callback`
	 * is bound to `thisArg` and invoked with 3 arguments; (value, index|key, collection).
	 * The `callback` argument may also be the name of a property to count by (e.g. 'length').
	 *
	 * @static
	 * @memberOf _
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Function|String} callback|property The function called per iteration
	 *  or property name to group by.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Object} Returns the composed aggregate object.
	 * @example
	 *
	 * _.groupBy([4.2, 6.1, 6.4], function(num) { return Math.floor(num); });
	 * // => { '4': [4.2], '6': [6.1, 6.4] }
	 *
	 * _.groupBy([4.2, 6.1, 6.4], function(num) { return this.floor(num); }, Math);
	 * // => { '4': [4.2], '6': [6.1, 6.4] }
	 *
	 * _.groupBy(['one', 'two', 'three'], 'length');
	 * // => { '3': ['one', 'two'], '5': ['three'] }
	 */
	var groupBy = createIterator(baseIteratorOptions, countByIteratorOptions, {
		'inLoop':
			'prop = callback(value, index, collection);\n' +
			'(hasOwnProperty.call(result, prop) ? result[prop] : result[prop] = []).push(value)'
	});

	/**
	 * Invokes the method named by `methodName` on each element in the `collection`.
	 * Additional arguments will be passed to each invoked method. If `methodName`
	 * is a function it will be invoked for, and `this` bound to, each element
	 * in the `collection`.
	 *
	 * @static
	 * @memberOf _
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Function|String} methodName The name of the method to invoke or
	 *  the function invoked per iteration.
	 * @param {Mixed} [arg1, arg2, ...] Arguments to invoke the method with.
	 * @returns {Array} Returns a new array of values returned from each invoked method.
	 * @example
	 *
	 * _.invoke([[5, 1, 7], [3, 2, 1]], 'sort');
	 * // => [[1, 5, 7], [1, 2, 3]]
	 *
	 * _.invoke([123, 456], String.prototype.split, '');
	 * // => [['1', '2', '3'], ['4', '5', '6']]
	 */
	var invoke = createIterator(mapIteratorOptions, {
		'args': 'collection, methodName',
		'top':
			'var args = slice.call(arguments, 2),\n' +
			'    isFunc = typeof methodName == \'function\'',
		'inLoop': {
			'array':
				'result[index] = (isFunc ? methodName : value[methodName]).apply(value, args)',
			'object':
				'result' + (isKeysFast ? '[ownIndex] = ' : '.push') +
				'((isFunc ? methodName : value[methodName]).apply(value, args))'
		}
	});

	/**
	 * Creates a new array of values by running each element in the `collection`
	 * through a `callback`. The `callback` is bound to `thisArg` and invoked with
	 * 3 arguments; (value, index|key, collection).
	 *
	 * @static
	 * @memberOf _
	 * @alias collect
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Function} [callback=identity] The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Array} Returns a new array of elements returned by the callback.
	 * @example
	 *
	 * _.map([1, 2, 3], function(num) { return num * 3; });
	 * // => [3, 6, 9]
	 *
	 * _.map({ 'one': 1, 'two': 2, 'three': 3 }, function(num) { return num * 3; });
	 * // => [3, 6, 9] (order is not guaranteed)
	 */
	var map = createIterator(baseIteratorOptions, mapIteratorOptions);

	/**
	 * Retrieves the value of a specified property from all elements in
	 * the `collection`.
	 *
	 * @static
	 * @memberOf _
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {String} property The property to pluck.
	 * @returns {Array} Returns a new array of property values.
	 * @example
	 *
	 * var stooges = [
	 *   { 'name': 'moe', 'age': 40 },
	 *   { 'name': 'larry', 'age': 50 },
	 *   { 'name': 'curly', 'age': 60 }
	 * ];
	 *
	 * _.pluck(stooges, 'name');
	 * // => ['moe', 'larry', 'curly']
	 */
	var pluck = createIterator(mapIteratorOptions, {
		'args': 'collection, property',
		'inLoop': {
			'array':  'result[index] = value[property]',
			'object': 'result' + (isKeysFast ? '[ownIndex] = ' : '.push') + '(value[property])'
		}
	});

	/**
	 * Boils down a `collection` to a single value. The initial state of the
	 * reduction is `accumulator` and each successive step of it should be returned
	 * by the `callback`. The `callback` is bound to `thisArg` and invoked with 4
	 * arguments; for arrays they are (accumulator, value, index|key, collection).
	 *
	 * @static
	 * @memberOf _
	 * @alias foldl, inject
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Function} callback The function called per iteration.
	 * @param {Mixed} [accumulator] Initial value of the accumulator.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Mixed} Returns the accumulated value.
	 * @example
	 *
	 * var sum = _.reduce([1, 2, 3], function(memo, num) { return memo + num; });
	 * // => 6
	 */
	var reduce = createIterator({
		'args': 'collection, callback, accumulator, thisArg',
		'init': 'accumulator',
		'top':
			'var noaccum = arguments.length < 3;\n' +
			'if (thisArg) callback = iteratorBind(callback, thisArg)',
		'beforeLoop': {
			'array': 'if (noaccum) result = iteratee[++index]'
		},
		'inLoop': {
			'array':
				'result = callback(result, value, index, collection)',
			'object':
				'result = noaccum\n' +
				'  ? (noaccum = false, value)\n' +
				'  : callback(result, value, index, collection)'
		}
	});

	/**
	 * The right-associative version of `_.reduce`.
	 *
	 * @static
	 * @memberOf _
	 * @alias foldr
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Function} callback The function called per iteration.
	 * @param {Mixed} [accumulator] Initial value of the accumulator.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Mixed} Returns the accumulated value.
	 * @example
	 *
	 * var list = [[0, 1], [2, 3], [4, 5]];
	 * var flat = _.reduceRight(list, function(a, b) { return a.concat(b); }, []);
	 * // => [4, 5, 2, 3, 0, 1]
	 */
	function reduceRight(collection, callback, accumulator, thisArg) {
		if (!collection) {
			return accumulator;
		}

		var length = collection.length,
				noaccum = arguments.length < 3;

		if(thisArg) {
			callback = iteratorBind(callback, thisArg);
		}
		// Opera 10.53-10.60 JITted `length >>> 0` returns the wrong value for negative numbers
		if (length > -1 && length === length >>> 0) {
			var iteratee = noCharByIndex && toString.call(collection) == stringClass
				? collection.split('')
				: collection;

			if (length && noaccum) {
				accumulator = iteratee[--length];
			}
			while (length--) {
				accumulator = callback(accumulator, iteratee[length], length, collection);
			}
			return accumulator;
		}

		var prop,
				props = keys(collection);

		length = props.length;
		if (length && noaccum) {
			accumulator = collection[props[--length]];
		}
		while (length--) {
			prop = props[length];
			accumulator = callback(accumulator, collection[prop], prop, collection);
		}
		return accumulator;
	}

	/**
	 * The opposite of `_.filter`, this method returns the values of a
	 * `collection` that `callback` does **not** return truthy for.
	 *
	 * @static
	 * @memberOf _
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Function} [callback=identity] The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Array} Returns a new array of elements that did **not** pass the callback check.
	 * @example
	 *
	 * var odds = _.reject([1, 2, 3, 4, 5, 6], function(num) { return num % 2 == 0; });
	 * // => [1, 3, 5]
	 */
	var reject = createIterator(baseIteratorOptions, filterIteratorOptions, {
		'inLoop': '!' + filterIteratorOptions.inLoop
	});

	/**
	 * Checks if the `callback` returns a truthy value for **any** element of a
	 * `collection`. The function returns as soon as it finds passing value, and
	 * does not iterate over the entire `collection`. The `callback` is bound to
	 * `thisArg` and invoked with 3 arguments; (value, index|key, collection).
	 *
	 * @static
	 * @memberOf _
	 * @alias any
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Function} [callback=identity] The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Boolean} Returns `true` if any element passes the callback check, else `false`.
	 * @example
	 *
	 * _.some([null, 0, 'yes', false]);
	 * // => true
	 */
	var some = createIterator(baseIteratorOptions, everyIteratorOptions, {
		'init': 'false',
		'inLoop': everyIteratorOptions.inLoop.replace('!', '')
	});

	/**
	 * Creates a new array, stable sorted in ascending order by the results of
	 * running each element of `collection` through a `callback`. The `callback`
	 * is bound to `thisArg` and invoked with 3 arguments; (value, index|key, collection).
	 * The `callback` argument may also be the name of a property to sort by (e.g. 'length').
	 *
	 * @static
	 * @memberOf _
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Function|String} callback|property The function called per iteration
	 *  or property name to sort by.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Array} Returns a new array of sorted elements.
	 * @example
	 *
	 * _.sortBy([1, 2, 3], function(num) { return Math.sin(num); });
	 * // => [3, 1, 2]
	 *
	 * _.sortBy([1, 2, 3], function(num) { return this.sin(num); }, Math);
	 * // => [3, 1, 2]
	 *
	 * _.sortBy(['larry', 'brendan', 'moe'], 'length');
	 * // => ['moe', 'larry', 'brendan']
	 */
	var sortBy = createIterator(baseIteratorOptions, countByIteratorOptions, mapIteratorOptions, {
		'inLoop': {
			'array':
				'result[index] = {\n' +
				'  criteria: callback(value, index, collection),\n' +
				'  index: index,\n' +
				'  value: value\n' +
				'}',
			'object':
				'result' + (isKeysFast ? '[ownIndex] = ' : '.push') + '({\n' +
				'  criteria: callback(value, index, collection),\n' +
				'  index: index,\n' +
				'  value: value\n' +
				'})'
		},
		'bottom':
			'result.sort(compareAscending);\n' +
			'length = result.length;\n' +
			'while (length--) {\n' +
			'  result[length] = result[length].value\n' +
			'}'
	});

	/**
	 * Converts the `collection`, to an array. Useful for converting the
	 * `arguments` object.
	 *
	 * @static
	 * @memberOf _
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to convert.
	 * @returns {Array} Returns the new converted array.
	 * @example
	 *
	 * (function() { return _.toArray(arguments).slice(1); })(1, 2, 3, 4);
	 * // => [2, 3, 4]
	 */
	function toArray(collection) {
		if (!collection) {
			return [];
		}
		if (collection.toArray && isFunction(collection.toArray)) {
			return collection.toArray();
		}
		var length = collection.length;
		if (length > -1 && length === length >>> 0) {
			return (noArraySliceOnStrings ? toString.call(collection) == stringClass : typeof collection == 'string')
				? collection.split('')
				: slice.call(collection);
		}
		return values(collection);
	}

	/**
	 * Examines each element in a `collection`, returning an array of all elements
	 * that contain the given `properties`.
	 *
	 * @static
	 * @memberOf _
	 * @category Collections
	 * @param {Array|Object|String} collection The collection to iterate over.
	 * @param {Object} properties The object of properties/values to filter by.
	 * @returns {Array} Returns a new array of elements that contain the given `properties`.
	 * @example
	 *
	 * var stooges = [
	 *   { 'name': 'moe', 'age': 40 },
	 *   { 'name': 'larry', 'age': 50 },
	 *   { 'name': 'curly', 'age': 60 }
	 * ];
	 *
	 * _.where(stooges, { 'age': 40 });
	 * // => [{ 'name': 'moe', 'age': 40 }]
	 */
	var where = createIterator(filterIteratorOptions, {
		'args': 'collection, properties',
		'top':
			'var props = [];\n' +
			'forIn(properties, function(value, prop) { props.push(prop) });\n' +
			'var propsLength = props.length',
		'inLoop':
			'for (var prop, pass = true, propIndex = 0; propIndex < propsLength; propIndex++) {\n' +
			'  prop = props[propIndex];\n' +
			'  if (!(pass = value[prop] === properties[prop])) break\n' +
			'}\n' +
			'pass && result.push(value)'
	});

	/*--------------------------------------------------------------------------*/

	/**
	 * Creates a new array with all falsey values of `array` removed. The values
	 * `false`, `null`, `0`, `""`, `undefined` and `NaN` are all falsey.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} array The array to compact.
	 * @returns {Array} Returns a new filtered array.
	 * @example
	 *
	 * _.compact([0, 1, false, 2, '', 3]);
	 * // => [1, 2, 3]
	 */
	function compact(array) {
		var result = [];
		if (!array) {
			return result;
		}
		var index = -1,
				length = array.length;

		while (++index < length) {
			if (array[index]) {
				result.push(array[index]);
			}
		}
		return result;
	}

	/**
	 * Creates a new array of `array` elements not present in the other arrays
	 * using strict equality for comparisons, i.e. `===`.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} array The array to process.
	 * @param {Array} [array1, array2, ...] Arrays to check.
	 * @returns {Array} Returns a new array of `array` elements not present in the
	 *  other arrays.
	 * @example
	 *
	 * _.difference([1, 2, 3, 4, 5], [5, 2, 10]);
	 * // => [1, 3, 4]
	 */
	function difference(array) {
		var result = [];
		if (!array) {
			return result;
		}
		var index = -1,
				length = array.length,
				flattened = concat.apply(result, arguments),
				contains = cachedContains(flattened, length);

		while (++index < length) {
			if (!contains(array[index])) {
				result.push(array[index]);
			}
		}
		return result;
	}

	/**
	 * Gets the first element of the `array`. Pass `n` to return the first `n`
	 * elements of the `array`.
	 *
	 * @static
	 * @memberOf _
	 * @alias head, take
	 * @category Arrays
	 * @param {Array} array The array to query.
	 * @param {Number} [n] The number of elements to return.
	 * @param {Object} [guard] Internally used to allow this method to work with
	 *  others like `_.map` without using their callback `index` argument for `n`.
	 * @returns {Mixed} Returns the first element or an array of the first `n`
	 *  elements of `array`.
	 * @example
	 *
	 * _.first([5, 4, 3, 2, 1]);
	 * // => 5
	 */
	function first(array, n, guard) {
		if (array) {
			return (n == null || guard) ? array[0] : slice.call(array, 0, n);
		}
	}

	/**
	 * Flattens a nested array (the nesting can be to any depth). If `shallow` is
	 * truthy, `array` will only be flattened a single level.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} array The array to compact.
	 * @param {Boolean} shallow A flag to indicate only flattening a single level.
	 * @returns {Array} Returns a new flattened array.
	 * @example
	 *
	 * _.flatten([1, [2], [3, [[4]]]]);
	 * // => [1, 2, 3, 4];
	 *
	 * _.flatten([1, [2], [3, [[4]]]], true);
	 * // => [1, 2, 3, [[4]]];
	 */
	function flatten(array, shallow) {
		var result = [];
		if (!array) {
			return result;
		}
		var value,
				index = -1,
				length = array.length;

		while (++index < length) {
			value = array[index];
			if (isArray(value)) {
				push.apply(result, shallow ? value : flatten(value));
			} else {
				result.push(value);
			}
		}
		return result;
	}

	/**
	 * Gets the index at which the first occurrence of `value` is found using
	 * strict equality for comparisons, i.e. `===`. If the `array` is already
	 * sorted, passing `true` for `isSorted` will run a faster binary search.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} array The array to search.
	 * @param {Mixed} value The value to search for.
	 * @param {Boolean|Number} [fromIndex=0] The index to start searching from or
	 *  `true` to perform a binary search on a sorted `array`.
	 * @returns {Number} Returns the index of the matched value or `-1`.
	 * @example
	 *
	 * _.indexOf([1, 2, 3, 1, 2, 3], 2);
	 * // => 1
	 *
	 * _.indexOf([1, 2, 3, 1, 2, 3], 2, 3);
	 * // => 4
	 *
	 * _.indexOf([1, 1, 2, 2, 3, 3], 2, true);
	 * // => 2
	 */
	function indexOf(array, value, fromIndex) {
		if (!array) {
			return -1;
		}
		var index = -1,
				length = array.length;

		if (fromIndex) {
			if (typeof fromIndex == 'number') {
				index = (fromIndex < 0 ? Math.max(0, length + fromIndex) : fromIndex) - 1;
			} else {
				index = sortedIndex(array, value);
				return array[index] === value ? index : -1;
			}
		}
		while (++index < length) {
			if (array[index] === value) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Gets all but the last element of `array`. Pass `n` to exclude the last `n`
	 * elements from the result.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} array The array to query.
	 * @param {Number} [n] The number of elements to return.
	 * @param {Object} [guard] Internally used to allow this method to work with
	 *  others like `_.map` without using their callback `index` argument for `n`.
	 * @returns {Array} Returns all but the last element or `n` elements of `array`.
	 * @example
	 *
	 * _.initial([3, 2, 1]);
	 * // => [3, 2]
	 */
	function initial(array, n, guard) {
		if (!array) {
			return [];
		}
		return slice.call(array, 0, -((n == null || guard) ? 1 : n));
	}

	/**
	 * Computes the intersection of all the passed-in arrays using strict equality
	 * for comparisons, i.e. `===`.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} [array1, array2, ...] Arrays to process.
	 * @returns {Array} Returns a new array of unique elements, in order, that are
	 *  present in **all** of the arrays.
	 * @example
	 *
	 * _.intersection([1, 2, 3], [101, 2, 1, 10], [2, 1]);
	 * // => [1, 2]
	 */
	function intersection(array) {
		var result = [];
		if (!array) {
			return result;
		}
		var value,
				argsLength = arguments.length,
				cache = [],
				index = -1,
				length = array.length;

		array: while (++index < length) {
			value = array[index];
			if (indexOf(result, value) < 0) {
				for (var argsIndex = 1; argsIndex < argsLength; argsIndex++) {
					if (!(cache[argsIndex] || (cache[argsIndex] = cachedContains(arguments[argsIndex])))(value)) {
						continue array;
					}
				}
				result.push(value);
			}
		}
		return result;
	}

	/**
	 * Gets the last element of the `array`. Pass `n` to return the lasy `n`
	 * elementsvof the `array`.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} array The array to query.
	 * @param {Number} [n] The number of elements to return.
	 * @param {Object} [guard] Internally used to allow this method to work with
	 *  others like `_.map` without using their callback `index` argument for `n`.
	 * @returns {Mixed} Returns the last element or an array of the last `n`
	 *  elements of `array`.
	 * @example
	 *
	 * _.last([3, 2, 1]);
	 * // => 1
	 */
	function last(array, n, guard) {
		if (array) {
			var length = array.length;
			return (n == null || guard) ? array[length - 1] : slice.call(array, -n || length);
		}
	}

	/**
	 * Gets the index at which the last occurrence of `value` is found using
	 * strict equality for comparisons, i.e. `===`.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} array The array to search.
	 * @param {Mixed} value The value to search for.
	 * @param {Number} [fromIndex=array.length-1] The index to start searching from.
	 * @returns {Number} Returns the index of the matched value or `-1`.
	 * @example
	 *
	 * _.lastIndexOf([1, 2, 3, 1, 2, 3], 2);
	 * // => 4
	 *
	 * _.lastIndexOf([1, 2, 3, 1, 2, 3], 2, 3);
	 * // => 1
	 */
	function lastIndexOf(array, value, fromIndex) {
		if (!array) {
			return -1;
		}
		var index = array.length;
		if (fromIndex && typeof fromIndex == 'number') {
			index = (fromIndex < 0 ? Math.max(0, index + fromIndex) : Math.min(fromIndex, index - 1)) + 1;
		}
		while (index--) {
			if (array[index] === value) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Retrieves the maximum value of an `array`. If `callback` is passed,
	 * it will be executed for each value in the `array` to generate the
	 * criterion by which the value is ranked. The `callback` is bound to
	 * `thisArg` and invoked with 3 arguments; (value, index, array).
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} array The array to iterate over.
	 * @param {Function} [callback] The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Mixed} Returns the maximum value.
	 * @example
	 *
	 * var stooges = [
	 *   { 'name': 'moe', 'age': 40 },
	 *   { 'name': 'larry', 'age': 50 },
	 *   { 'name': 'curly', 'age': 60 }
	 * ];
	 *
	 * _.max(stooges, function(stooge) { return stooge.age; });
	 * // => { 'name': 'curly', 'age': 60 };
	 */
	function max(array, callback, thisArg) {
		var computed = -Infinity,
				result = computed;

		if (!array) {
			return result;
		}
		var current,
				index = -1,
				length = array.length;

		if (!callback) {
			while (++index < length) {
				if (array[index] > result) {
					result = array[index];
				}
			}
			return result;
		}
		if (thisArg) {
			callback = iteratorBind(callback, thisArg);
		}
		while (++index < length) {
			current = callback(array[index], index, array);
			if (current > computed) {
				computed = current;
				result = array[index];
			}
		}
		return result;
	}

	/**
	 * Retrieves the minimum value of an `array`. If `callback` is passed,
	 * it will be executed for each value in the `array` to generate the
	 * criterion by which the value is ranked. The `callback` is bound to `thisArg`
	 * and invoked with 3 arguments; (value, index, array).
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} array The array to iterate over.
	 * @param {Function} [callback] The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Mixed} Returns the minimum value.
	 * @example
	 *
	 * _.min([10, 5, 100, 2, 1000]);
	 * // => 2
	 */
	function min(array, callback, thisArg) {
		var computed = Infinity,
				result = computed;

		if (!array) {
			return result;
		}
		var current,
				index = -1,
				length = array.length;

		if (!callback) {
			while (++index < length) {
				if (array[index] < result) {
					result = array[index];
				}
			}
			return result;
		}
		if (thisArg) {
			callback = iteratorBind(callback, thisArg);
		}
		while (++index < length) {
			current = callback(array[index], index, array);
			if (current < computed) {
				computed = current;
				result = array[index];
			}
		}
		return result;
	}

	/**
	 * Creates an array of numbers (positive and/or negative) progressing from
	 * `start` up to but not including `stop`. This method is a port of Python's
	 * `range()` function. See http://docs.python.org/library/functions.html#range.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Number} [start=0] The start of the range.
	 * @param {Number} end The end of the range.
	 * @param {Number} [step=1] The value to increment or descrement by.
	 * @returns {Array} Returns a new range array.
	 * @example
	 *
	 * _.range(10);
	 * // => [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
	 *
	 * _.range(1, 11);
	 * // => [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
	 *
	 * _.range(0, 30, 5);
	 * // => [0, 5, 10, 15, 20, 25]
	 *
	 * _.range(0, -10, -1);
	 * // => [0, -1, -2, -3, -4, -5, -6, -7, -8, -9]
	 *
	 * _.range(0);
	 * // => []
	 */
	function range(start, end, step) {
		start = +start || 0;
		step = +step || 1;

		if (end == null) {
			end = start;
			start = 0;
		}
		// use `Array(length)` so V8 will avoid the slower "dictionary" mode
		// http://www.youtube.com/watch?v=XAqIpGU8ZZk#t=16m27s
		var index = -1,
				length = Math.max(0, Math.ceil((end - start) / step)),
				result = Array(length);

		while (++index < length) {
			result[index] = start;
			start += step;
		}
		return result;
	}

	/**
	 * The opposite of `_.initial`, this method gets all but the first value of
	 * `array`. Pass `n` to exclude the first `n` values from the result.
	 *
	 * @static
	 * @memberOf _
	 * @alias tail
	 * @category Arrays
	 * @param {Array} array The array to query.
	 * @param {Number} [n] The number of elements to return.
	 * @param {Object} [guard] Internally used to allow this method to work with
	 *  others like `_.map` without using their callback `index` argument for `n`.
	 * @returns {Array} Returns all but the first value or `n` values of `array`.
	 * @example
	 *
	 * _.rest([3, 2, 1]);
	 * // => [2, 1]
	 */
	function rest(array, n, guard) {
		if (!array) {
			return [];
		}
		return slice.call(array, (n == null || guard) ? 1 : n);
	}

	/**
	 * Creates a new array of shuffled `array` values, using a version of the
	 * Fisher-Yates shuffle. See http://en.wikipedia.org/wiki/Fisher-Yates_shuffle.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} array The array to shuffle.
	 * @returns {Array} Returns a new shuffled array.
	 * @example
	 *
	 * _.shuffle([1, 2, 3, 4, 5, 6]);
	 * // => [4, 1, 6, 3, 5, 2]
	 */
	function shuffle(array) {
		if (!array) {
			return [];
		}
		var rand,
				index = -1,
				length = array.length,
				result = Array(length);

		while (++index < length) {
			rand = Math.floor(Math.random() * (index + 1));
			result[index] = result[rand];
			result[rand] = array[index];
		}
		return result;
	}

	/**
	 * Uses a binary search to determine the smallest index at which the `value`
	 * should be inserted into `array` in order to maintain the sort order of the
	 * sorted `array`. If `callback` is passed, it will be executed for `value` and
	 * each element in `array` to compute their sort ranking. The `callback` is
	 * bound to `thisArg` and invoked with 1 argument; (value).
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} array The array to iterate over.
	 * @param {Mixed} value The value to evaluate.
	 * @param {Function} [callback=identity] The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Number} Returns the index at which the value should be inserted
	 *  into `array`.
	 * @example
	 *
	 * _.sortedIndex([20, 30, 40], 35);
	 * // => 2
	 *
	 * var dict = {
	 *   'wordToNumber': { 'twenty': 20, 'thirty': 30, 'thirty-five': 35, 'fourty': 40 }
	 * };
	 *
	 * _.sortedIndex(['twenty', 'thirty', 'fourty'], 'thirty-five', function(word) {
	 *   return dict.wordToNumber[word];
	 * });
	 * // => 2
	 *
	 * _.sortedIndex(['twenty', 'thirty', 'fourty'], 'thirty-five', function(word) {
	 *   return this.wordToNumber[word];
	 * }, dict);
	 * // => 2
	 */
	function sortedIndex(array, value, callback, thisArg) {
		if (!array) {
			return 0;
		}
		var mid,
				low = 0,
				high = array.length;

		if (callback) {
			if (thisArg) {
				callback = bind(callback, thisArg);
			}
			value = callback(value);
			while (low < high) {
				mid = (low + high) >>> 1;
				callback(array[mid]) < value ? low = mid + 1 : high = mid;
			}
		} else {
			while (low < high) {
				mid = (low + high) >>> 1;
				array[mid] < value ? low = mid + 1 : high = mid;
			}
		}
		return low;
	}

	/**
	 * Computes the union of the passed-in arrays using strict equality for
	 * comparisons, i.e. `===`.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} [array1, array2, ...] Arrays to process.
	 * @returns {Array} Returns a new array of unique values, in order, that are
	 *  present in one or more of the arrays.
	 * @example
	 *
	 * _.union([1, 2, 3], [101, 2, 1, 10], [2, 1]);
	 * // => [1, 2, 3, 101, 10]
	 */
	function union() {
		var index = -1,
				result = [],
				flattened = concat.apply(result, arguments),
				length = flattened.length;

		while (++index < length) {
			if (indexOf(result, flattened[index]) < 0) {
				result.push(flattened[index]);
			}
		}
		return result;
	}

	/**
	 * Creates a duplicate-value-free version of the `array` using strict equality
	 * for comparisons, i.e. `===`. If the `array` is already sorted, passing `true`
	 * for `isSorted` will run a faster algorithm. If `callback` is passed, each
	 * element of `array` is passed through a callback` before uniqueness is computed.
	 * The `callback` is bound to `thisArg` and invoked with 3 arguments; (value, index, array).
	 *
	 * @static
	 * @memberOf _
	 * @alias unique
	 * @category Arrays
	 * @param {Array} array The array to process.
	 * @param {Boolean} [isSorted=false] A flag to indicate that the `array` is already sorted.
	 * @param {Function} [callback=identity] The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @returns {Array} Returns a duplicate-value-free array.
	 * @example
	 *
	 * _.uniq([1, 2, 1, 3, 1]);
	 * // => [1, 2, 3]
	 *
	 * _.uniq([1, 1, 2, 2, 3], true);
	 * // => [1, 2, 3]
	 *
	 * _.uniq([1, 2, 1.5, 3, 2.5], function(num) { return Math.floor(num); });
	 * // => [1, 2, 3]
	 *
	 * _.uniq([1, 2, 1.5, 3, 2.5], function(num) { return this.floor(num); }, Math);
	 * // => [1, 2, 3]
	 */
	function uniq(array, isSorted, callback, thisArg) {
		var result = [];
		if (!array) {
			return result;
		}
		var computed,
				index = -1,
				length = array.length,
				seen = [];

		// juggle arguments
		if (typeof isSorted == 'function') {
			thisArg = callback;
			callback = isSorted;
			isSorted = false;
		}
		if (!callback) {
			callback = identity;
		} else if (thisArg) {
			callback = iteratorBind(callback, thisArg);
		}
		while (++index < length) {
			computed = callback(array[index], index, array);
			if (isSorted
						? !index || seen[seen.length - 1] !== computed
						: indexOf(seen, computed) < 0
					) {
				seen.push(computed);
				result.push(array[index]);
			}
		}
		return result;
	}

	/**
	 * Creates a new array with all occurrences of the passed values removed using
	 * strict equality for comparisons, i.e. `===`.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} array The array to filter.
	 * @param {Mixed} [value1, value2, ...] Values to remove.
	 * @returns {Array} Returns a new filtered array.
	 * @example
	 *
	 * _.without([1, 2, 1, 0, 3, 1, 4], 0, 1);
	 * // => [2, 3, 4]
	 */
	function without(array) {
		var result = [];
		if (!array) {
			return result;
		}
		var index = -1,
				length = array.length,
				contains = cachedContains(arguments, 1, 20);

		while (++index < length) {
			if (!contains(array[index])) {
				result.push(array[index]);
			}
		}
		return result;
	}

	/**
	 * Groups the elements of each array at their corresponding indexes. Useful for
	 * separate data sources that are coordinated through matching array indexes.
	 * For a matrix of nested arrays, `_.zip.apply(...)` can transpose the matrix
	 * in a similar fashion.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} [array1, array2, ...] Arrays to process.
	 * @returns {Array} Returns a new array of grouped elements.
	 * @example
	 *
	 * _.zip(['moe', 'larry', 'curly'], [30, 40, 50], [true, false, false]);
	 * // => [['moe', 30, true], ['larry', 40, false], ['curly', 50, false]]
	 */
	function zip(array) {
		if (!array) {
			return [];
		}
		var index = -1,
				length = max(pluck(arguments, 'length')),
				result = Array(length);

		while (++index < length) {
			result[index] = pluck(arguments, index);
		}
		return result;
	}

	/**
	 * Creates an object composed from an array of `keys` and an array of `values`.
	 *
	 * @static
	 * @memberOf _
	 * @category Arrays
	 * @param {Array} keys The array of keys.
	 * @param {Array} [values=[]] The array of values.
	 * @returns {Object} Returns an object composed of the given keys and
	 *  corresponding values.
	 * @example
	 *
	 * _.zipObject(['moe', 'larry', 'curly'], [30, 40, 50]);
	 * // => { 'moe': 30, 'larry': 40, 'curly': 50 }
	 */
	function zipObject(keys, values) {
		if (!keys) {
			return {};
		}
		var index = -1,
				length = keys.length,
				result = {};

		values || (values = []);
		while (++index < length) {
			result[keys[index]] = values[index];
		}
		return result;
	}

	/*--------------------------------------------------------------------------*/

	/**
	 * Creates a new function that is restricted to executing only after it is
	 * called `n` times.
	 *
	 * @static
	 * @memberOf _
	 * @category Functions
	 * @param {Number} n The number of times the function must be called before
	 * it is executed.
	 * @param {Function} func The function to restrict.
	 * @returns {Function} Returns the new restricted function.
	 * @example
	 *
	 * var renderNotes = _.after(notes.length, render);
	 * _.forEach(notes, function(note) {
	 *   note.asyncSave({ 'success': renderNotes });
	 * });
	 * // `renderNotes` is run once, after all notes have saved
	 */
	function after(n, func) {
		if (n < 1) {
			return func();
		}
		return function() {
			if (--n < 1) {
				return func.apply(this, arguments);
			}
		};
	}

	/**
	 * Creates a new function that, when called, invokes `func` with the `this`
	 * binding of `thisArg` and prepends any additional `bind` arguments to those
	 * passed to the bound function. Lazy defined methods may be bound by passing
	 * the object they are bound to as `func` and the method name as `thisArg`.
	 *
	 * @static
	 * @memberOf _
	 * @category Functions
	 * @param {Function|Object} func The function to bind or the object the method belongs to.
	 * @param {Mixed} [thisArg] The `this` binding of `func` or the method name.
	 * @param {Mixed} [arg1, arg2, ...] Arguments to be partially applied.
	 * @returns {Function} Returns the new bound function.
	 * @example
	 *
	 * // basic bind
	 * var func = function(greeting) {
	 *   return greeting + ' ' + this.name;
	 * };
	 *
	 * func = _.bind(func, { 'name': 'moe' }, 'hi');
	 * func();
	 * // => 'hi moe'
	 *
	 * // lazy bind
	 * var object = {
	 *   'name': 'moe',
	 *   'greet': function(greeting) {
	 *     return greeting + ' ' + this.name;
	 *   }
	 * };
	 *
	 * var func = _.bind(object, 'greet', 'hi');
	 * func();
	 * // => 'hi moe'
	 *
	 * object.greet = function(greeting) {
	 *   return greeting + ', ' + this.name + '!';
	 * };
	 *
	 * func();
	 * // => 'hi, moe!'
	 */
	function bind(func, thisArg) {
		var methodName,
				isFunc = isFunction(func);

		// juggle arguments
		if (!isFunc) {
			methodName = thisArg;
			thisArg = func;
		}
		// use `Function#bind` if it exists and is fast
		// (in V8 `Function#bind` is slower except when partially applied)
		else if (isBindFast || (nativeBind && arguments.length > 2)) {
			return nativeBind.call.apply(nativeBind, arguments);
		}

		var partialArgs = slice.call(arguments, 2);

		function bound() {
			// `Function#bind` spec
			// http://es5.github.com/#x15.3.4.5
			var args = arguments,
					thisBinding = thisArg;

			if (!isFunc) {
				func = thisArg[methodName];
			}
			if (partialArgs.length) {
				args = args.length
					? partialArgs.concat(slice.call(args))
					: partialArgs;
			}
			if (this instanceof bound) {
				// get `func` instance if `bound` is invoked in a `new` expression
				noop.prototype = func.prototype;
				thisBinding = new noop;

				// mimic the constructor's `return` behavior
				// http://es5.github.com/#x13.2.2
				var result = func.apply(thisBinding, args);
				return result && objectTypes[typeof result]
					? result
					: thisBinding
			}
			return func.apply(thisBinding, args);
		}
		return bound;
	}

	/**
	 * Binds methods on `object` to `object`, overwriting the existing method.
	 * If no method names are provided, all the function properties of `object`
	 * will be bound.
	 *
	 * @static
	 * @memberOf _
	 * @category Functions
	 * @param {Object} object The object to bind and assign the bound methods to.
	 * @param {String} [methodName1, methodName2, ...] Method names on the object to bind.
	 * @returns {Object} Returns `object`.
	 * @example
	 *
	 * var buttonView = {
	 *  'label': 'lodash',
	 *  'onClick': function() { alert('clicked: ' + this.label); }
	 * };
	 *
	 * _.bindAll(buttonView);
	 * jQuery('#lodash_button').on('click', buttonView.onClick);
	 * // => When the button is clicked, `this.label` will have the correct value
	 */
	var bindAll = createIterator({
		'useHas': false,
		'useStrict': false,
		'args': 'object',
		'init': 'object',
		'top':
			'var funcs = arguments,\n' +
			'    length = funcs.length;\n' +
			'if (length > 1) {\n' +
			'  for (var index = 1; index < length; index++) {\n' +
			'    result[funcs[index]] = bind(result[funcs[index]], result)\n' +
			'  }\n' +
			'  return result\n' +
			'}',
		'inLoop':
			'if (isFunction(result[index])) {\n' +
			'  result[index] = bind(result[index], result)\n' +
			'}'
	});

	/**
	 * Creates a new function that is the composition of the passed functions,
	 * where each function consumes the return value of the function that follows.
	 * In math terms, composing the functions `f()`, `g()`, and `h()` produces `f(g(h()))`.
	 *
	 * @static
	 * @memberOf _
	 * @category Functions
	 * @param {Function} [func1, func2, ...] Functions to compose.
	 * @returns {Function} Returns the new composed function.
	 * @example
	 *
	 * var greet = function(name) { return 'hi: ' + name; };
	 * var exclaim = function(statement) { return statement + '!'; };
	 * var welcome = _.compose(exclaim, greet);
	 * welcome('moe');
	 * // => 'hi: moe!'
	 */
	function compose() {
		var funcs = arguments;
		return function() {
			var args = arguments,
					length = funcs.length;

			while (length--) {
				args = [funcs[length].apply(this, args)];
			}
			return args[0];
		};
	}

	/**
	 * Creates a new function that will delay the execution of `func` until after
	 * `wait` milliseconds have elapsed since the last time it was invoked. Pass
	 * `true` for `immediate` to cause debounce to invoke `func` on the leading,
	 * instead of the trailing, edge of the `wait` timeout. Subsequent calls to
	 * the debounced function will return the result of the last `func` call.
	 *
	 * @static
	 * @memberOf _
	 * @category Functions
	 * @param {Function} func The function to debounce.
	 * @param {Number} wait The number of milliseconds to delay.
	 * @param {Boolean} immediate A flag to indicate execution is on the leading
	 *  edge of the timeout.
	 * @returns {Function} Returns the new debounced function.
	 * @example
	 *
	 * var lazyLayout = _.debounce(calculateLayout, 300);
	 * jQuery(window).on('resize', lazyLayout);
	 */
	function debounce(func, wait, immediate) {
		var args,
				result,
				thisArg,
				timeoutId;

		function delayed() {
			timeoutId = null;
			if (!immediate) {
				func.apply(thisArg, args);
			}
		}

		return function() {
			var isImmediate = immediate && !timeoutId;
			args = arguments;
			thisArg = this;

			clearTimeout(timeoutId);
			timeoutId = setTimeout(delayed, wait);

			if (isImmediate) {
				result = func.apply(thisArg, args);
			}
			return result;
		};
	}

	/**
	 * Executes the `func` function after `wait` milliseconds. Additional arguments
	 * will be passed to `func` when it is invoked.
	 *
	 * @static
	 * @memberOf _
	 * @category Functions
	 * @param {Function} func The function to delay.
	 * @param {Number} wait The number of milliseconds to delay execution.
	 * @param {Mixed} [arg1, arg2, ...] Arguments to invoke the function with.
	 * @returns {Number} Returns the `setTimeout` timeout id.
	 * @example
	 *
	 * var log = _.bind(console.log, console);
	 * _.delay(log, 1000, 'logged later');
	 * // => 'logged later' (Appears after one second.)
	 */
	function delay(func, wait) {
		var args = slice.call(arguments, 2);
		return setTimeout(function() { return func.apply(undefined, args); }, wait);
	}

	/**
	 * Defers executing the `func` function until the current call stack has cleared.
	 * Additional arguments will be passed to `func` when it is invoked.
	 *
	 * @static
	 * @memberOf _
	 * @category Functions
	 * @param {Function} func The function to defer.
	 * @param {Mixed} [arg1, arg2, ...] Arguments to invoke the function with.
	 * @returns {Number} Returns the `setTimeout` timeout id.
	 * @example
	 *
	 * _.defer(function() { alert('deferred'); });
	 * // returns from the function before `alert` is called
	 */
	function defer(func) {
		var args = slice.call(arguments, 1);
		return setTimeout(function() { return func.apply(undefined, args); }, 1);
	}

	/**
	 * Creates a new function that memoizes the result of `func`. If `resolver` is
	 * passed, it will be used to determine the cache key for storing the result
	 * based on the arguments passed to the memoized function. By default, the first
	 * argument passed to the memoized function is used as the cache key.
	 *
	 * @static
	 * @memberOf _
	 * @category Functions
	 * @param {Function} func The function to have its output memoized.
	 * @param {Function} [resolver] A function used to resolve the cache key.
	 * @returns {Function} Returns the new memoizing function.
	 * @example
	 *
	 * var fibonacci = _.memoize(function(n) {
	 *   return n < 2 ? n : fibonacci(n - 1) + fibonacci(n - 2);
	 * });
	 */
	function memoize(func, resolver) {
		var cache = {};
		return function() {
			var prop = resolver ? resolver.apply(this, arguments) : arguments[0];
			return hasOwnProperty.call(cache, prop)
				? cache[prop]
				: (cache[prop] = func.apply(this, arguments));
		};
	}

	/**
	 * Creates a new function that is restricted to one execution. Repeat calls to
	 * the function will return the value of the first call.
	 *
	 * @static
	 * @memberOf _
	 * @category Functions
	 * @param {Function} func The function to restrict.
	 * @returns {Function} Returns the new restricted function.
	 * @example
	 *
	 * var initialize = _.once(createApplication);
	 * initialize();
	 * initialize();
	 * // Application is only created once.
	 */
	function once(func) {
		var result,
				ran = false;

		return function() {
			if (ran) {
				return result;
			}
			ran = true;
			result = func.apply(this, arguments);

			// clear the `func` variable so the function may be garbage collected
			func = null;
			return result;
		};
	}

	/**
	 * Creates a new function that, when called, invokes `func` with any additional
	 * `partial` arguments prepended to those passed to the new function. This method
	 * is similar `bind`, except it does **not** alter the `this` binding.
	 *
	 * @static
	 * @memberOf _
	 * @category Functions
	 * @param {Function} func The function to partially apply arguments to.
	 * @param {Mixed} [arg1, arg2, ...] Arguments to be partially applied.
	 * @returns {Function} Returns the new partially applied function.
	 * @example
	 *
	 * var greet = function(greeting, name) { return greeting + ': ' + name; };
	 * var hi = _.partial(greet, 'hi');
	 * hi('moe');
	 * // => 'hi: moe'
	 */
	function partial(func) {
		var args = slice.call(arguments, 1),
				argsLength = args.length;

		return function() {
			var result,
					others = arguments;

			if (others.length) {
				args.length = argsLength;
				push.apply(args, others);
			}
			result = args.length == 1 ? func.call(this, args[0]) : func.apply(this, args);
			args.length = argsLength;
			return result;
		};
	}

	/**
	 * Creates a new function that, when executed, will only call the `func`
	 * function at most once per every `wait` milliseconds. If the throttled
	 * function is invoked more than once during the `wait` timeout, `func` will
	 * also be called on the trailing edge of the timeout. Subsequent calls to the
	 * throttled function will return the result of the last `func` call.
	 *
	 * @static
	 * @memberOf _
	 * @category Functions
	 * @param {Function} func The function to throttle.
	 * @param {Number} wait The number of milliseconds to throttle executions to.
	 * @returns {Function} Returns the new throttled function.
	 * @example
	 *
	 * var throttled = _.throttle(updatePosition, 100);
	 * jQuery(window).on('scroll', throttled);
	 */
	function throttle(func, wait) {
		var args,
				result,
				thisArg,
				timeoutId,
				lastCalled = 0;

		function trailingCall() {
			lastCalled = new Date;
			timeoutId = null;
			func.apply(thisArg, args);
		}

		return function() {
			var now = new Date,
					remain = wait - (now - lastCalled);

			args = arguments;
			thisArg = this;

			if (remain <= 0) {
				lastCalled = now;
				result = func.apply(thisArg, args);
			}
			else if (!timeoutId) {
				timeoutId = setTimeout(trailingCall, remain);
			}
			return result;
		};
	}

	/**
	 * Creates a new function that passes `value` to the `wrapper` function as its
	 * first argument. Additional arguments passed to the new function are appended
	 * to those passed to the `wrapper` function.
	 *
	 * @static
	 * @memberOf _
	 * @category Functions
	 * @param {Mixed} value The value to wrap.
	 * @param {Function} wrapper The wrapper function.
	 * @returns {Function} Returns the new function.
	 * @example
	 *
	 * var hello = function(name) { return 'hello: ' + name; };
	 * hello = _.wrap(hello, function(func) {
	 *   return 'before, ' + func('moe') + ', after';
	 * });
	 * hello();
	 * // => 'before, hello: moe, after'
	 */
	function wrap(value, wrapper) {
		return function() {
			var args = [value];
			if (arguments.length) {
				push.apply(args, arguments);
			}
			return wrapper.apply(this, args);
		};
	}

	/*--------------------------------------------------------------------------*/

	/**
	 * Converts the characters `&`, `<`, `>`, `"`, and `'` in `string` to their
	 * corresponding HTML entities.
	 *
	 * @static
	 * @memberOf _
	 * @category Utilities
	 * @param {String} string The string to escape.
	 * @returns {String} Returns the escaped string.
	 * @example
	 *
	 * _.escape('Moe, Larry & Curly');
	 * // => "Moe, Larry &amp; Curly"
	 */
	function escape(string) {
		return string == null ? '' : (string + '').replace(reUnescapedHtml, escapeHtmlChar);
	}

	/**
	 * This function returns the first argument passed to it.
	 *
	 * Note: It is used throughout Lo-Dash as a default callback.
	 *
	 * @static
	 * @memberOf _
	 * @category Utilities
	 * @param {Mixed} value Any value.
	 * @returns {Mixed} Returns `value`.
	 * @example
	 *
	 * var moe = { 'name': 'moe' };
	 * moe === _.identity(moe);
	 * // => true
	 */
	function identity(value) {
		return value;
	}

	/**
	 * Adds functions properties of `object` to the `lodash` function and chainable
	 * wrapper.
	 *
	 * @static
	 * @memberOf _
	 * @category Utilities
	 * @param {Object} object The object of function properties to add to `lodash`.
	 * @example
	 *
	 * _.mixin({
	 *   'capitalize': function(string) {
	 *     return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
	 *   }
	 * });
	 *
	 * _.capitalize('larry');
	 * // => 'Larry'
	 *
	 * _('curly').capitalize();
	 * // => 'Curly'
	 */
	function mixin(object) {
		forEach(functions(object), function(methodName) {
			var func = lodash[methodName] = object[methodName];

			LoDash.prototype[methodName] = function() {
				var args = [this._wrapped];
				if (arguments.length) {
					push.apply(args, arguments);
				}
				var result = func.apply(lodash, args);
				if (this._chain) {
					result = new LoDash(result);
					result._chain = true;
				}
				return result;
			};
		});
	}

	/**
	 * Reverts the '_' variable to its previous value and returns a reference to
	 * the `lodash` function.
	 *
	 * @static
	 * @memberOf _
	 * @category Utilities
	 * @returns {Function} Returns the `lodash` function.
	 * @example
	 *
	 * var lodash = _.noConflict();
	 */
	function noConflict() {
		window._ = oldDash;
		return this;
	}

	/**
	 * Resolves the value of `property` on `object`. If `property` is a function
	 * it will be invoked and its result returned, else the property value is
	 * returned. If `object` is falsey, then `null` is returned.
	 *
	 * @deprecated
	 * @static
	 * @memberOf _
	 * @category Utilities
	 * @param {Object} object The object to inspect.
	 * @param {String} property The property to get the result of.
	 * @returns {Mixed} Returns the resolved value.
	 * @example
	 *
	 * var object = {
	 *   'cheese': 'crumpets',
	 *   'stuff': function() {
	 *     return 'nonsense';
	 *   }
	 * };
	 *
	 * _.result(object, 'cheese');
	 * // => 'crumpets'
	 *
	 * _.result(object, 'stuff');
	 * // => 'nonsense'
	 */
	function result(object, property) {
		// based on Backbone's private `getValue` function
		// https://github.com/documentcloud/backbone/blob/0.9.2/backbone.js#L1419-1424
		if (!object) {
			return null;
		}
		var value = object[property];
		return isFunction(value) ? object[property]() : value;
	}

	/**
	 * A micro-templating method that handles arbitrary delimiters, preserves
	 * whitespace, and correctly escapes quotes within interpolated code.
	 *
	 * Note: In the development build `_.template` utilizes sourceURLs for easier
	 * debugging. See http://www.html5rocks.com/en/tutorials/developertools/sourcemaps/#toc-sourceurl
	 *
	 * Note: Lo-Dash may be used in Chrome extensions by either creating a `lodash csp`
	 * build and avoiding `_.template` use, or loading Lo-Dash in a sandboxed page.
	 * See http://developer.chrome.com/trunk/extensions/sandboxingEval.html
	 *
	 * @static
	 * @memberOf _
	 * @category Utilities
	 * @param {String} text The template text.
	 * @param {Obect} data The data object used to populate the text.
	 * @param {Object} options The options object.
	 * @returns {Function|String} Returns a compiled function when no `data` object
	 *  is given, else it returns the interpolated text.
	 * @example
	 *
	 * // using a compiled template
	 * var compiled = _.template('hello: <%= name %>');
	 * compiled({ 'name': 'moe' });
	 * // => 'hello: moe'
	 *
	 * var list = '<% _.forEach(people, function(name) { %> <li><%= name %></li> <% }); %>';
	 * _.template(list, { 'people': ['moe', 'larry', 'curly'] });
	 * // => '<li>moe</li><li>larry</li><li>curly</li>'
	 *
	 * // using the "escape" delimiter to escape HTML in data property values
	 * _.template('<b><%- value %></b>', { 'value': '<script>' });
	 * // => '<b>&lt;script></b>'
	 *
	 * // using the internal `print` function in "evaluate" delimiters
	 * _.template('<% print("Hello " + epithet); %>', { 'epithet': 'stooge' });
	 * // => 'Hello stooge.'
	 *
	 * // using custom template delimiter settings
	 * _.templateSettings = {
	 *   'interpolate': /\{\{(.+?)\}\}/g
	 * };
	 *
	 * _.template('Hello {{ name }}!', { 'name': 'Mustache' });
	 * // => 'Hello Mustache!'
	 *
	 * // using the `variable` option to ensure a with-statement isn't used in the compiled template
	 * var compiled = _.template('hello: <%= data.name %>', null, { 'variable': 'data' });
	 * compiled.source;
	 * // => function(data) {
	 *   var __t, __p = '', __e = _.escape;
	 *   __p += 'hello: ' + ((__t = ( data.name )) == null ? '' : __t);
	 *   return __p;
	 * }
	 *
	 * // using the `source` property to inline compiled templates for meaningful
	 * // line numbers in error messages and a stack trace
	 * fs.writeFileSync(path.join(cwd, 'jst.js'), '\
	 *   var JST = {\
	 *     "main": ' + _.template(mainText).source + '\
	 *   };\
	 * ');
	 */
	function template(text, data, options) {
		// based on John Resig's `tmpl` implementation
		// http://ejohn.org/blog/javascript-micro-templating/
		// and Laura Doktorova's doT.js
		// https://github.com/olado/doT
		options || (options = {});
		text += '';

		var isEvaluating,
				result,
				escapeDelimiter = options.escape,
				evaluateDelimiter = options.evaluate,
				interpolateDelimiter = options.interpolate,
				settings = lodash.templateSettings,
				variable = options.variable || settings.variable,
				hasVariable = variable;

		// use default settings if no options object is provided
		if (escapeDelimiter == null) {
			escapeDelimiter = settings.escape;
		}
		if (evaluateDelimiter == null) {
			// use `false` as the fallback value, instead of leaving it `undefined`,
			// so the initial assignment of `reEvaluateDelimiter` will still occur
			evaluateDelimiter = settings.evaluate || false;
		}
		if (interpolateDelimiter == null) {
			interpolateDelimiter = settings.interpolate;
		}

		// tokenize delimiters to avoid escaping them
		if (escapeDelimiter) {
			text = text.replace(escapeDelimiter, tokenizeEscape);
		}
		if (interpolateDelimiter) {
			text = text.replace(interpolateDelimiter, tokenizeInterpolate);
		}
		if (evaluateDelimiter != lastEvaluateDelimiter) {
			// generate `reEvaluateDelimiter` to match `_.templateSettings.evaluate`
			// and internal `<e%- %>`, `<e%= %>` delimiters
			lastEvaluateDelimiter = evaluateDelimiter;
			reEvaluateDelimiter = RegExp(
				'<e%-([\\s\\S]+?)%>|<e%=([\\s\\S]+?)%>' +
				(evaluateDelimiter ? '|' + evaluateDelimiter.source : '')
			, 'g');
		}
		isEvaluating = tokenized.length;
		text = text.replace(reEvaluateDelimiter, tokenizeEvaluate);
		isEvaluating = isEvaluating != tokenized.length;

		// escape characters that cannot be included in string literals and
		// detokenize delimiter code snippets
		text = "__p += '" + text
			.replace(reUnescapedString, escapeStringChar)
			.replace(reToken, detokenize) + "';\n";

		// clear stored code snippets
		tokenized.length = 0;

		// if `variable` is not specified and the template contains "evaluate"
		// delimiters, wrap a with-statement around the generated code to add the
		// data object to the top of the scope chain
		if (!hasVariable) {
			variable = lastVariable || 'obj';

			if (isEvaluating) {
				text = 'with (' + variable + ') {\n' + text + '\n}\n';
			}
			else {
				if (variable != lastVariable) {
					// generate `reDoubleVariable` to match references like `obj.obj` inside
					// transformed "escape" and "interpolate" delimiters
					lastVariable = variable;
					reDoubleVariable = RegExp('(\\(\\s*)' + variable + '\\.' + variable + '\\b', 'g');
				}
				// avoid a with-statement by prepending data object references to property names
				text = text
					.replace(reInsertVariable, '$&' + variable + '.')
					.replace(reDoubleVariable, '$1__d');
			}
		}

		// cleanup code by stripping empty strings
		text = ( isEvaluating ? text.replace(reEmptyStringLeading, '') : text)
			.replace(reEmptyStringMiddle, '$1')
			.replace(reEmptyStringTrailing, '$1;');

		// frame code as the function body
		text = 'function(' + variable + ') {\n' +
			(hasVariable ? '' : variable + ' || (' + variable + ' = {});\n') +
			'var __t, __p = \'\', __e = _.escape' +
			(isEvaluating
				? ', __j = Array.prototype.join;\n' +
					'function print() { __p += __j.call(arguments, \'\') }\n'
				: (hasVariable ? '' : ', __d = ' + variable + '.' + variable + ' || ' + variable) + ';\n'
			) +
			text +
			'return __p\n}';

		// add a sourceURL for easier debugging
		// http://www.html5rocks.com/en/tutorials/developertools/sourcemaps/#toc-sourceurl
		if (useSourceURL) {
			text += '\n//@ sourceURL=/lodash/template/source[' + (templateCounter++) + ']';
		}

		try {
			result = Function('_', 'return ' + text)(lodash);
		} catch(e) {
			// defer syntax errors until the compiled template is executed to allow
			// examining the `source` property beforehand and for consistency,
			// because other template related errors occur at execution
			result = function() { throw e; };
		}

		if (data) {
			return result(data);
		}
		// provide the compiled function's source via its `toString` method, in
		// supported environments, or the `source` property as a convenience for
		// inlining compiled templates during the build process
		result.source = text;
		return result;
	}

	/**
	 * Executes the `callback` function `n` times. The `callback` is bound to
	 * `thisArg` and invoked with 1 argument; (index).
	 *
	 * @static
	 * @memberOf _
	 * @category Utilities
	 * @param {Number} n The number of times to execute the callback.
	 * @param {Function} callback The function called per iteration.
	 * @param {Mixed} [thisArg] The `this` binding for the callback.
	 * @example
	 *
	 * _.times(3, function() { genie.grantWish(); });
	 * // => calls `genie.grantWish()` 3 times
	 *
	 * _.times(3, function() { this.grantWish(); }, genie);
	 * // => also calls `genie.grantWish()` 3 times
	 */
	function times(n, callback, thisArg) {
		var index = -1;
		if (thisArg) {
			while (++index < n) {
				callback.call(thisArg, index);
			}
		} else {
			while (++index < n) {
				callback(index);
			}
		}
	}

	/**
	 * Converts the HTML entities `&amp;`, `&lt;`, `&gt;`, `&quot;`, and `&#x27;`
	 * in `string` to their corresponding characters.
	 *
	 * @static
	 * @memberOf _
	 * @category Utilities
	 * @param {String} string The string to unescape.
	 * @returns {String} Returns the unescaped string.
	 * @example
	 *
	 * _.unescape('Moe, Larry &amp; Curly');
	 * // => "Moe, Larry & Curly"
	 */
	function unescape(string) {
		return string == null ? '' : (string + '').replace(reEscapedHtml, unescapeHtmlChar);
	}

	/**
	 * Generates a unique id. If `prefix` is passed, the id will be appended to it.
	 *
	 * @static
	 * @memberOf _
	 * @category Utilities
	 * @param {String} [prefix] The value to prefix the id with.
	 * @returns {Number|String} Returns a numeric id if no prefix is passed, else
	 *  a string id may be returned.
	 * @example
	 *
	 * _.uniqueId('contact_');
	 * // => 'contact_104'
	 */
	function uniqueId(prefix) {
		var id = idCounter++;
		return prefix ? prefix + id : id;
	}

	/*--------------------------------------------------------------------------*/

	/**
	 * Wraps the value in a `lodash` wrapper object.
	 *
	 * @static
	 * @memberOf _
	 * @category Chaining
	 * @param {Mixed} value The value to wrap.
	 * @returns {Object} Returns the wrapper object.
	 * @example
	 *
	 * var stooges = [
	 *   { 'name': 'moe', 'age': 40 },
	 *   { 'name': 'larry', 'age': 50 },
	 *   { 'name': 'curly', 'age': 60 }
	 * ];
	 *
	 * var youngest = _.chain(stooges)
	 *     .sortBy(function(stooge) { return stooge.age; })
	 *     .map(function(stooge) { return stooge.name + ' is ' + stooge.age; })
	 *     .first()
	 *     .value();
	 * // => 'moe is 40'
	 */
	function chain(value) {
		value = new LoDash(value);
		value._chain = true;
		return value;
	}

	/**
	 * Invokes `interceptor` with the `value` as the first argument, and then
	 * returns `value`. The purpose of this method is to "tap into" a method chain,
	 * in order to perform operations on intermediate results within the chain.
	 *
	 * @static
	 * @memberOf _
	 * @category Chaining
	 * @param {Mixed} value The value to pass to `interceptor`.
	 * @param {Function} interceptor The function to invoke.
	 * @returns {Mixed} Returns `value`.
	 * @example
	 *
	 * _.chain([1,2,3,200])
	 *  .filter(function(num) { return num % 2 == 0; })
	 *  .tap(alert)
	 *  .map(function(num) { return num * num })
	 *  .value();
	 * // => // [2, 200] (alerted)
	 * // => [4, 40000]
	 */
	function tap(value, interceptor) {
		interceptor(value);
		return value;
	}

	/**
	 * Enables method chaining on the wrapper object.
	 *
	 * @name chain
	 * @deprecated
	 * @memberOf _
	 * @category Chaining
	 * @returns {Mixed} Returns the wrapper object.
	 * @example
	 *
	 * _([1, 2, 3]).value();
	 * // => [1, 2, 3]
	 */
	function wrapperChain() {
		this._chain = true;
		return this;
	}

	/**
	 * Extracts the wrapped value.
	 *
	 * @name value
	 * @memberOf _
	 * @category Chaining
	 * @returns {Mixed} Returns the wrapped value.
	 * @example
	 *
	 * _([1, 2, 3]).value();
	 * // => [1, 2, 3]
	 */
	function wrapperValue() {
		return this._wrapped;
	}

	/*--------------------------------------------------------------------------*/

	/**
	 * The semantic version number.
	 *
	 * @static
	 * @memberOf _
	 * @type String
	 */
	lodash.VERSION = '0.6.1';

	// assign static methods
	lodash.after = after;
	lodash.bind = bind;
	lodash.bindAll = bindAll;
	lodash.chain = chain;
	lodash.clone = clone;
	lodash.compact = compact;
	lodash.compose = compose;
	lodash.contains = contains;
	lodash.countBy = countBy;
	lodash.debounce = debounce;
	lodash.defaults = defaults;
	lodash.defer = defer;
	lodash.delay = delay;
	lodash.difference = difference;
	lodash.drop = drop;
	lodash.escape = escape;
	lodash.every = every;
	lodash.extend = extend;
	lodash.filter = filter;
	lodash.find = find;
	lodash.first = first;
	lodash.flatten = flatten;
	lodash.forEach = forEach;
	lodash.forIn = forIn;
	lodash.forOwn = forOwn;
	lodash.functions = functions;
	lodash.groupBy = groupBy;
	lodash.has = has;
	lodash.identity = identity;
	lodash.indexOf = indexOf;
	lodash.initial = initial;
	lodash.intersection = intersection;
	lodash.invoke = invoke;
	lodash.isArguments = isArguments;
	lodash.isArray = isArray;
	lodash.isBoolean = isBoolean;
	lodash.isDate = isDate;
	lodash.isElement = isElement;
	lodash.isEmpty = isEmpty;
	lodash.isEqual = isEqual;
	lodash.isFinite = isFinite;
	lodash.isFunction = isFunction;
	lodash.isNaN = isNaN;
	lodash.isNull = isNull;
	lodash.isNumber = isNumber;
	lodash.isObject = isObject;
	lodash.isRegExp = isRegExp;
	lodash.isString = isString;
	lodash.isUndefined = isUndefined;
	lodash.keys = keys;
	lodash.last = last;
	lodash.lastIndexOf = lastIndexOf;
	lodash.map = map;
	lodash.max = max;
	lodash.memoize = memoize;
	lodash.merge = merge;
	lodash.min = min;
	lodash.mixin = mixin;
	lodash.noConflict = noConflict;
	lodash.once = once;
	lodash.partial = partial;
	lodash.pick = pick;
	lodash.pluck = pluck;
	lodash.range = range;
	lodash.reduce = reduce;
	lodash.reduceRight = reduceRight;
	lodash.reject = reject;
	lodash.rest = rest;
	lodash.result = result;
	lodash.shuffle = shuffle;
	lodash.size = size;
	lodash.some = some;
	lodash.sortBy = sortBy;
	lodash.sortedIndex = sortedIndex;
	lodash.tap = tap;
	lodash.template = template;
	lodash.throttle = throttle;
	lodash.times = times;
	lodash.toArray = toArray;
	lodash.unescape = unescape;
	lodash.union = union;
	lodash.uniq = uniq;
	lodash.uniqueId = uniqueId;
	lodash.values = values;
	lodash.where = where;
	lodash.without = without;
	lodash.wrap = wrap;
	lodash.zip = zip;
	lodash.zipObject = zipObject;

	// assign aliases
	lodash.all = every;
	lodash.any = some;
	lodash.collect = map;
	lodash.detect = find;
	lodash.each = forEach;
	lodash.foldl = reduce;
	lodash.foldr = reduceRight;
	lodash.head = first;
	lodash.include = contains;
	lodash.inject = reduce;
	lodash.methods = functions;
	lodash.omit = drop;
	lodash.select = filter;
	lodash.tail = rest;
	lodash.take = first;
	lodash.unique = uniq;

	// add pseudo private properties used and removed during the build process
	lodash._iteratorTemplate = iteratorTemplate;
	lodash._shimKeys = shimKeys;

	/*--------------------------------------------------------------------------*/

	// assign private `LoDash` constructor's prototype
	LoDash.prototype = lodash.prototype;

	// add all static functions to `LoDash.prototype`
	mixin(lodash);

	// add `LoDash.prototype.chain` after calling `mixin()` to avoid overwriting
	// it with the wrapped `lodash.chain`
	LoDash.prototype.chain = wrapperChain;
	LoDash.prototype.value = wrapperValue;

	// add all mutator Array functions to the wrapper.
	forEach(['pop', 'push', 'reverse', 'shift', 'sort', 'splice', 'unshift'], function(methodName) {
		var func = ArrayProto[methodName];

		LoDash.prototype[methodName] = function() {
			var value = this._wrapped;
			func.apply(value, arguments);

			// avoid array-like object bugs with `Array#shift` and `Array#splice` in
			// Firefox < 10 and IE < 9
			if (hasObjectSpliceBug && value.length === 0) {
				delete value[0];
			}
			if (this._chain) {
				value = new LoDash(value);
				value._chain = true;
			}
			return value;
		};
	});

	// add all accessor Array functions to the wrapper.
	forEach(['concat', 'join', 'slice'], function(methodName) {
		var func = ArrayProto[methodName];

		LoDash.prototype[methodName] = function() {
			var value = this._wrapped,
					result = func.apply(value, arguments);

			if (this._chain) {
				result = new LoDash(result);
				result._chain = true;
			}
			return result;
		};
	});

	/*--------------------------------------------------------------------------*/

	// expose Lo-Dash
	// some AMD build optimizers, like r.js, check for specific condition patterns like the following:
	if (typeof define == 'function' && typeof define.amd == 'object' && define.amd) {
		// Expose Lo-Dash to the global object even when an AMD loader is present in
		// case Lo-Dash was injected by a third-party script and not intended to be
		// loaded as a module. The global assignment can be reverted in the Lo-Dash
		// module via its `noConflict()` method.
		window._ = lodash;

		// define as an anonymous module so, through path mapping, it can be
		// referenced as the "underscore" module
		define(function() {
			return lodash;
		});
	}
	// check for `exports` after `define` in case a build optimizer adds an `exports` object
	else if (freeExports) {
		// in Node.js or RingoJS v0.8.0+
		if (typeof module == 'object' && module && module.exports == freeExports) {
			(module.exports = lodash)._ = lodash;
		}
		// in Narwhal or RingoJS v0.7.0-
		else {
			freeExports._ = lodash;
		}
	}
	else {
		// in a browser or Rhino
		window._ = lodash;
	}
}(this));

/**
 * jQuery Validation Plugin 1.9.0
 *
 * http://bassistance.de/jquery-plugins/jquery-plugin-validation/
 * http://docs.jquery.com/Plugins/Validation
 *
 * Copyright (c) 2006 - 2011 JÃ¶rn Zaefferer
 *
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 */
(function($) {

$.extend($.fn, {
	// http://docs.jquery.com/Plugins/Validation/validate
	validate: function( options ) {

		// if nothing is selected, return nothing; can't chain anyway
		if (!this.length) {
			options && options.debug && window.console && console.warn( "nothing selected, can't validate, returning nothing" );
			return;
		}

		// check if a validator for this form was already created
		var validator = $.data(this[0], 'validator');
		if ( validator ) {
			return validator;
		}

		// Add novalidate tag if HTML5.
		this.attr('novalidate', 'novalidate');

		validator = new $.validator( options, this[0] );
		$.data(this[0], 'validator', validator);

		if ( validator.settings.onsubmit ) {

			var inputsAndButtons = this.find("input, button");

			// allow suppresing validation by adding a cancel class to the submit button
			inputsAndButtons.filter(".cancel").click(function () {
				validator.cancelSubmit = true;
			});

			// when a submitHandler is used, capture the submitting button
			if (validator.settings.submitHandler) {
				inputsAndButtons.filter(":submit").click(function () {
					validator.submitButton = this;
				});
			}

			// validate the form on submit
			this.submit( function( event ) {
				if ( validator.settings.debug )
					// prevent form submit to be able to see console output
					event.preventDefault();

				function handle() {
					if ( validator.settings.submitHandler ) {
						if (validator.submitButton) {
							// insert a hidden input as a replacement for the missing submit button
							var hidden = $("<input type='hidden'/>").attr("name", validator.submitButton.name).val(validator.submitButton.value).appendTo(validator.currentForm);
						}
						validator.settings.submitHandler.call( validator, validator.currentForm );
						if (validator.submitButton) {
							// and clean up afterwards; thanks to no-block-scope, hidden can be referenced
							hidden.remove();
						}
						return false;
					}
					return true;
				}

				// prevent submit for invalid forms or custom submit handlers
				if ( validator.cancelSubmit ) {
					validator.cancelSubmit = false;
					return handle();
				}
				if ( validator.form() ) {
					if ( validator.pendingRequest ) {
						validator.formSubmitted = true;
						return false;
					}
					return handle();
				} else {
					validator.focusInvalid();
					return false;
				}
			});
		}

		return validator;
	},
	// http://docs.jquery.com/Plugins/Validation/valid
	valid: function() {
		if ( $(this[0]).is('form')) {
			return this.validate().form();
		} else {
			var valid = true;
			var validator = $(this[0].form).validate();
			this.each(function() {
				valid &= validator.element(this);
			});
			return valid;
		}
	},
	// attributes: space seperated list of attributes to retrieve and remove
	removeAttrs: function(attributes) {
		var result = {},
			$element = this;
		$.each(attributes.split(/\s/), function(index, value) {
			result[value] = $element.attr(value);
			$element.removeAttr(value);
		});
		return result;
	},
	// http://docs.jquery.com/Plugins/Validation/rules
	rules: function(command, argument) {
		var element = this[0];

		if (command) {
			var settings = $.data(element.form, 'validator').settings;
			var staticRules = settings.rules;
			var existingRules = $.validator.staticRules(element);
			switch(command) {
			case "add":
				$.extend(existingRules, $.validator.normalizeRule(argument));
				staticRules[element.name] = existingRules;
				if (argument.messages)
					settings.messages[element.name] = $.extend( settings.messages[element.name], argument.messages );
				break;
			case "remove":
				if (!argument) {
					delete staticRules[element.name];
					return existingRules;
				}
				var filtered = {};
				$.each(argument.split(/\s/), function(index, method) {
					filtered[method] = existingRules[method];
					delete existingRules[method];
				});
				return filtered;
			}
		}

		var data = $.validator.normalizeRules(
		$.extend(
			{},
			$.validator.metadataRules(element),
			$.validator.classRules(element),
			$.validator.attributeRules(element),
			$.validator.staticRules(element)
		), element);

		// make sure required is at front
		if (data.required) {
			var param = data.required;
			delete data.required;
			data = $.extend({required: param}, data);
		}

		return data;
	}
});

// Custom selectors
$.extend($.expr[":"], {
	// http://docs.jquery.com/Plugins/Validation/blank
	blank: function(a) {return !$.trim("" + a.value);},
	// http://docs.jquery.com/Plugins/Validation/filled
	filled: function(a) {return !!$.trim("" + a.value);},
	// http://docs.jquery.com/Plugins/Validation/unchecked
	unchecked: function(a) {return !a.checked;}
});

// constructor for validator
$.validator = function( options, form ) {
	this.settings = $.extend( true, {}, $.validator.defaults, options );
	this.currentForm = form;
	this.init();
};

$.validator.format = function(source, params) {
	if ( arguments.length == 1 )
		return function() {
			var args = $.makeArray(arguments);
			args.unshift(source);
			return $.validator.format.apply( this, args );
		};
	if ( arguments.length > 2 && params.constructor != Array  ) {
		params = $.makeArray(arguments).slice(1);
	}
	if ( params.constructor != Array ) {
		params = [ params ];
	}
	$.each(params, function(i, n) {
		source = source.replace(new RegExp("\\{" + i + "\\}", "g"), n);
	});
	return source;
};

$.extend($.validator, {

	defaults: {
		messages: {},
		groups: {},
		rules: {},
		errorClass: "error",
		validClass: "valid",
		errorElement: "label",
		focusInvalid: true,
		errorContainer: $( [] ),
		errorLabelContainer: $( [] ),
		onsubmit: true,
		ignore: ":hidden",
		ignoreTitle: false,
		onfocusin: function(element, event) {
			this.lastActive = element;

			// hide error label and remove error class on focus if enabled
			if ( this.settings.focusCleanup && !this.blockFocusCleanup ) {
				this.settings.unhighlight && this.settings.unhighlight.call( this, element, this.settings.errorClass, this.settings.validClass );
				this.addWrapper(this.errorsFor(element)).hide();
			}
		},
		onfocusout: function(element, event) {
			if ( !this.checkable(element) && (element.name in this.submitted || !this.optional(element)) ) {
				this.element(element);
			}
		},
		onkeyup: function(element, event) {
			if ( element.name in this.submitted || element == this.lastElement ) {
				this.element(element);
			}
		},
		onclick: function(element, event) {
			// click on selects, radiobuttons and checkboxes
			if ( element.name in this.submitted )
				this.element(element);
			// or option elements, check parent select in that case
			else if (element.parentNode.name in this.submitted)
				this.element(element.parentNode);
		},
		highlight: function(element, errorClass, validClass) {
			if (element.type === 'radio') {
				this.findByName(element.name).addClass(errorClass).removeClass(validClass);
			} else {
				$(element).addClass(errorClass).removeClass(validClass);
			}
		},
		unhighlight: function(element, errorClass, validClass) {
			if (element.type === 'radio') {
				this.findByName(element.name).removeClass(errorClass).addClass(validClass);
			} else {
				$(element).removeClass(errorClass).addClass(validClass);
			}
		}
	},

	// http://docs.jquery.com/Plugins/Validation/Validator/setDefaults
	setDefaults: function(settings) {
		$.extend( $.validator.defaults, settings );
	},

	messages: {
		required: "This field is required.",
		remote: "Please fix this field.",
		email: "Please enter a valid email address.",
		url: "Please enter a valid URL.",
		date: "Please enter a valid date.",
		dateISO: "Please enter a valid date (ISO).",
		number: "Please enter a valid number.",
		digits: "Please enter only digits.",
		creditcard: "Please enter a valid credit card number.",
		equalTo: "Please enter the same value again.",
		accept: "Please enter a value with a valid extension.",
		maxlength: $.validator.format("Please enter no more than {0} characters."),
		minlength: $.validator.format("Please enter at least {0} characters."),
		rangelength: $.validator.format("Please enter a value between {0} and {1} characters long."),
		range: $.validator.format("Please enter a value between {0} and {1}."),
		max: $.validator.format("Please enter a value less than or equal to {0}."),
		min: $.validator.format("Please enter a value greater than or equal to {0}.")
	},

	autoCreateRanges: false,

	prototype: {

		init: function() {
			this.labelContainer = $(this.settings.errorLabelContainer);
			this.errorContext = this.labelContainer.length && this.labelContainer || $(this.currentForm);
			this.containers = $(this.settings.errorContainer).add( this.settings.errorLabelContainer );
			this.submitted = {};
			this.valueCache = {};
			this.pendingRequest = 0;
			this.pending = {};
			this.invalid = {};
			this.reset();

			var groups = (this.groups = {});
			$.each(this.settings.groups, function(key, value) {
				$.each(value.split(/\s/), function(index, name) {
					groups[name] = key;
				});
			});
			var rules = this.settings.rules;
			$.each(rules, function(key, value) {
				rules[key] = $.validator.normalizeRule(value);
			});

			function delegate(event) {
				var validator = $.data(this[0].form, "validator"),
					eventType = "on" + event.type.replace(/^validate/, "");
				validator.settings[eventType] && validator.settings[eventType].call(validator, this[0], event);
			}
			$(this.currentForm)
				   .validateDelegate("[type='text'], [type='password'], [type='file'], select, textarea, " +
						"[type='number'], [type='search'] ,[type='tel'], [type='url'], " +
						"[type='email'], [type='datetime'], [type='date'], [type='month'], " +
						"[type='week'], [type='time'], [type='datetime-local'], " +
						"[type='range'], [type='color'] ",
						"focusin focusout keyup", delegate)
				.validateDelegate("[type='radio'], [type='checkbox'], select, option", "click", delegate);

			if (this.settings.invalidHandler)
				$(this.currentForm).bind("invalid-form.validate", this.settings.invalidHandler);
		},

		// http://docs.jquery.com/Plugins/Validation/Validator/form
		form: function() {
			this.checkForm();
			$.extend(this.submitted, this.errorMap);
			this.invalid = $.extend({}, this.errorMap);
			if (!this.valid())
				$(this.currentForm).triggerHandler("invalid-form", [this]);
			this.showErrors();
			return this.valid();
		},

		checkForm: function() {
			this.prepareForm();
			for ( var i = 0, elements = (this.currentElements = this.elements()); elements[i]; i++ ) {
				this.check( elements[i] );
			}
			return this.valid();
		},

		// http://docs.jquery.com/Plugins/Validation/Validator/element
		element: function( element ) {
			element = this.validationTargetFor( this.clean( element ) );
			this.lastElement = element;
			this.prepareElement( element );
			this.currentElements = $(element);
			var result = this.check( element );
			if ( result ) {
				delete this.invalid[element.name];
			} else {
				this.invalid[element.name] = true;
			}
			if ( !this.numberOfInvalids() ) {
				// Hide error containers on last error
				this.toHide = this.toHide.add( this.containers );
			}
			this.showErrors();
			return result;
		},

		// http://docs.jquery.com/Plugins/Validation/Validator/showErrors
		showErrors: function(errors) {
			if(errors) {
				// add items to error list and map
				$.extend( this.errorMap, errors );
				this.errorList = [];
				for ( var name in errors ) {
					this.errorList.push({
						message: errors[name],
						element: this.findByName(name)[0]
					});
				}
				// remove items from success list
				this.successList = $.grep( this.successList, function(element) {
					return !(element.name in errors);
				});
			}
			this.settings.showErrors
				? this.settings.showErrors.call( this, this.errorMap, this.errorList )
				: this.defaultShowErrors();
		},

		// http://docs.jquery.com/Plugins/Validation/Validator/resetForm
		resetForm: function() {
			if ( $.fn.resetForm )
				$( this.currentForm ).resetForm();
			this.submitted = {};
			this.lastElement = null;
			this.prepareForm();
			this.hideErrors();
			this.elements().removeClass( this.settings.errorClass );
		},

		numberOfInvalids: function() {
			return this.objectLength(this.invalid);
		},

		objectLength: function( obj ) {
			var count = 0;
			for ( var i in obj )
				count++;
			return count;
		},

		hideErrors: function() {
			this.addWrapper( this.toHide ).hide();
		},

		valid: function() {
			return this.size() == 0;
		},

		size: function() {
			return this.errorList.length;
		},

		focusInvalid: function() {
			if( this.settings.focusInvalid ) {
				try {
					$(this.findLastActive() || this.errorList.length && this.errorList[0].element || [])
					.filter(":visible")
					.focus()
					// manually trigger focusin event; without it, focusin handler isn't called, findLastActive won't have anything to find
					.trigger("focusin");
				} catch(e) {
					// ignore IE throwing errors when focusing hidden elements
				}
			}
		},

		findLastActive: function() {
			var lastActive = this.lastActive;
			return lastActive && $.grep(this.errorList, function(n) {
				return n.element.name == lastActive.name;
			}).length == 1 && lastActive;
		},

		elements: function() {
			var validator = this,
				rulesCache = {};

			// select all valid inputs inside the form (no submit or reset buttons)
			return $(this.currentForm)
			.find("input, select, textarea")
			.not(":submit, :reset, :image, [disabled]")
			.not( this.settings.ignore )
			.filter(function() {
				!this.name && validator.settings.debug && window.console && console.error( "%o has no name assigned", this);

				// select only the first element for each name, and only those with rules specified
				if ( this.name in rulesCache || !validator.objectLength($(this).rules()) )
					return false;

				rulesCache[this.name] = true;
				return true;
			});
		},

		clean: function( selector ) {
			return $( selector )[0];
		},

		errors: function() {
			return $( this.settings.errorElement + "." + this.settings.errorClass, this.errorContext );
		},

		reset: function() {
			this.successList = [];
			this.errorList = [];
			this.errorMap = {};
			this.toShow = $([]);
			this.toHide = $([]);
			this.currentElements = $([]);
		},

		prepareForm: function() {
			this.reset();
			this.toHide = this.errors().add( this.containers );
		},

		prepareElement: function( element ) {
			this.reset();
			this.toHide = this.errorsFor(element);
		},

		check: function( element ) {
			element = this.validationTargetFor( this.clean( element ) );

			var rules = $(element).rules();
			var dependencyMismatch = false;
			for (var method in rules ) {
				var rule = { method: method, parameters: rules[method] };
				try {
					var result = $.validator.methods[method].call( this, element.value.replace(/\r/g, ""), element, rule.parameters );

					// if a method indicates that the field is optional and therefore valid,
					// don't mark it as valid when there are no other rules
					if ( result == "dependency-mismatch" ) {
						dependencyMismatch = true;
						continue;
					}
					dependencyMismatch = false;

					if ( result == "pending" ) {
						this.toHide = this.toHide.not( this.errorsFor(element) );
						return;
					}

					if( !result ) {
						this.formatAndAdd( element, rule );
						return false;
					}
				} catch(e) {
					this.settings.debug && window.console && console.log("exception occured when checking element " + element.id
						 + ", check the '" + rule.method + "' method", e);
					throw e;
				}
			}
			if (dependencyMismatch)
				return;
			if ( this.objectLength(rules) )
				this.successList.push(element);
			return true;
		},

		// return the custom message for the given element and validation method
		// specified in the element's "messages" metadata
		customMetaMessage: function(element, method) {
			if (!$.metadata)
				return;

			var meta = this.settings.meta
				? $(element).metadata()[this.settings.meta]
				: $(element).metadata();

			return meta && meta.messages && meta.messages[method];
		},

		// return the custom message for the given element name and validation method
		customMessage: function( name, method ) {
			var m = this.settings.messages[name];
			return m && (m.constructor == String
				? m
				: m[method]);
		},

		// return the first defined argument, allowing empty strings
		findDefined: function() {
			for(var i = 0; i < arguments.length; i++) {
				if (arguments[i] !== undefined)
					return arguments[i];
			}
			return undefined;
		},

		defaultMessage: function( element, method) {
			return this.findDefined(
				this.customMessage( element.name, method ),
				this.customMetaMessage( element, method ),
				// title is never undefined, so handle empty string as undefined
				!this.settings.ignoreTitle && element.title || undefined,
				$.validator.messages[method],
				"<strong>Warning: No message defined for " + element.name + "</strong>"
			);
		},

		formatAndAdd: function( element, rule ) {
			var message = this.defaultMessage( element, rule.method ),
				theregex = /\$?\{(\d+)\}/g;
			if ( typeof message == "function" ) {
				message = message.call(this, rule.parameters, element);
			} else if (theregex.test(message)) {
				message = jQuery.format(message.replace(theregex, '{$1}'), rule.parameters);
			}
			this.errorList.push({
				message: message,
				element: element
			});

			this.errorMap[element.name] = message;
			this.submitted[element.name] = message;
		},

		addWrapper: function(toToggle) {
			if ( this.settings.wrapper )
				toToggle = toToggle.add( toToggle.parent( this.settings.wrapper ) );
			return toToggle;
		},

		defaultShowErrors: function() {
			for ( var i = 0; this.errorList[i]; i++ ) {
				var error = this.errorList[i];
				this.settings.highlight && this.settings.highlight.call( this, error.element, this.settings.errorClass, this.settings.validClass );
				this.showLabel( error.element, error.message );
			}
			if( this.errorList.length ) {
				this.toShow = this.toShow.add( this.containers );
			}
			if (this.settings.success) {
				for ( var i = 0; this.successList[i]; i++ ) {
					this.showLabel( this.successList[i] );
				}
			}
			if (this.settings.unhighlight) {
				for ( var i = 0, elements = this.validElements(); elements[i]; i++ ) {
					this.settings.unhighlight.call( this, elements[i], this.settings.errorClass, this.settings.validClass );
				}
			}
			this.toHide = this.toHide.not( this.toShow );
			this.hideErrors();
			this.addWrapper( this.toShow ).show();
		},

		validElements: function() {
			return this.currentElements.not(this.invalidElements());
		},

		invalidElements: function() {
			return $(this.errorList).map(function() {
				return this.element;
			});
		},

		showLabel: function(element, message) {
			var label = this.errorsFor( element );
			if ( label.length ) {
				// refresh error/success class
				label.removeClass( this.settings.validClass ).addClass( this.settings.errorClass );

				// check if we have a generated label, replace the message then
				label.attr("generated") && label.html(message);
			} else {
				// create label
				label = $("<" + this.settings.errorElement + "/>")
					.attr({"for":  this.idOrName(element), generated: true})
					.addClass(this.settings.errorClass)
					.html(message || "");
				if ( this.settings.wrapper ) {
					// make sure the element is visible, even in IE
					// actually showing the wrapped element is handled elsewhere
					label = label.hide().show().wrap("<" + this.settings.wrapper + "/>").parent();
				}
				if ( !this.labelContainer.append(label).length )
					this.settings.errorPlacement
						? this.settings.errorPlacement(label, $(element) )
						: label.insertAfter(element);
			}
			if ( !message && this.settings.success ) {
				label.text("");
				typeof this.settings.success == "string"
					? label.addClass( this.settings.success )
					: this.settings.success( label );
			}
			this.toShow = this.toShow.add(label);
		},

		errorsFor: function(element) {
			var name = this.idOrName(element);
			return this.errors().filter(function() {
				return $(this).attr('for') == name;
			});
		},

		idOrName: function(element) {
			return this.groups[element.name] || (this.checkable(element) ? element.name : element.id || element.name);
		},

		validationTargetFor: function(element) {
			// if radio/checkbox, validate first element in group instead
			if (this.checkable(element)) {
				element = this.findByName( element.name ).not(this.settings.ignore)[0];
			}
			return element;
		},

		checkable: function( element ) {
			return /radio|checkbox/i.test(element.type);
		},

		findByName: function( name ) {
			// select by name and filter by form for performance over form.find("[name=...]")
			var form = this.currentForm;
			return $(document.getElementsByName(name)).map(function(index, element) {
				return element.form == form && element.name == name && element  || null;
			});
		},

		getLength: function(value, element) {
			switch( element.nodeName.toLowerCase() ) {
			case 'select':
				return $("option:selected", element).length;
			case 'input':
				if( this.checkable( element) )
					return this.findByName(element.name).filter(':checked').length;
			}
			return value.length;
		},

		depend: function(param, element) {
			return this.dependTypes[typeof param]
				? this.dependTypes[typeof param](param, element)
				: true;
		},

		dependTypes: {
			"boolean": function(param, element) {
				return param;
			},
			"string": function(param, element) {
				return !!$(param, element.form).length;
			},
			"function": function(param, element) {
				return param(element);
			}
		},

		optional: function(element) {
			return !$.validator.methods.required.call(this, $.trim(element.value), element) && "dependency-mismatch";
		},

		startRequest: function(element) {
			if (!this.pending[element.name]) {
				this.pendingRequest++;
				this.pending[element.name] = true;
			}
		},

		stopRequest: function(element, valid) {
			this.pendingRequest--;
			// sometimes synchronization fails, make sure pendingRequest is never < 0
			if (this.pendingRequest < 0)
				this.pendingRequest = 0;
			delete this.pending[element.name];
			if ( valid && this.pendingRequest == 0 && this.formSubmitted && this.form() ) {
				$(this.currentForm).submit();
				this.formSubmitted = false;
			} else if (!valid && this.pendingRequest == 0 && this.formSubmitted) {
				$(this.currentForm).triggerHandler("invalid-form", [this]);
				this.formSubmitted = false;
			}
		},

		previousValue: function(element) {
			return $.data(element, "previousValue") || $.data(element, "previousValue", {
				old: null,
				valid: true,
				message: this.defaultMessage( element, "remote" )
			});
		}

	},

	classRuleSettings: {
		required: {required: true},
		email: {email: true},
		url: {url: true},
		date: {date: true},
		dateISO: {dateISO: true},
		dateDE: {dateDE: true},
		number: {number: true},
		numberDE: {numberDE: true},
		digits: {digits: true},
		creditcard: {creditcard: true}
	},

	addClassRules: function(className, rules) {
		className.constructor == String ?
			this.classRuleSettings[className] = rules :
			$.extend(this.classRuleSettings, className);
	},

	classRules: function(element) {
		var rules = {};
		var classes = $(element).attr('class');
		classes && $.each(classes.split(' '), function() {
			if (this in $.validator.classRuleSettings) {
				$.extend(rules, $.validator.classRuleSettings[this]);
			}
		});
		return rules;
	},

	attributeRules: function(element) {
		var rules = {};
		var $element = $(element);

		for (var method in $.validator.methods) {
			var value;
			// If .prop exists (jQuery >= 1.6), use it to get true/false for required
			if (method === 'required' && typeof $.fn.prop === 'function') {
				value = $element.prop(method);
			} else {
				value = $element.attr(method);
			}
			if (value) {
				rules[method] = value;
			} else if ($element[0].getAttribute("type") === method) {
				rules[method] = true;
			}
		}

		// maxlength may be returned as -1, 2147483647 (IE) and 524288 (safari) for text inputs
		if (rules.maxlength && /-1|2147483647|524288/.test(rules.maxlength)) {
			delete rules.maxlength;
		}

		return rules;
	},

	metadataRules: function(element) {
		if (!$.metadata) return {};

		var meta = $.data(element.form, 'validator').settings.meta;
		return meta ?
			$(element).metadata()[meta] :
			$(element).metadata();
	},

	staticRules: function(element) {
		var rules = {};
		var validator = $.data(element.form, 'validator');
		if (validator.settings.rules) {
			rules = $.validator.normalizeRule(validator.settings.rules[element.name]) || {};
		}
		return rules;
	},

	normalizeRules: function(rules, element) {
		// handle dependency check
		$.each(rules, function(prop, val) {
			// ignore rule when param is explicitly false, eg. required:false
			if (val === false) {
				delete rules[prop];
				return;
			}
			if (val.param || val.depends) {
				var keepRule = true;
				switch (typeof val.depends) {
					case "string":
						keepRule = !!$(val.depends, element.form).length;
						break;
					case "function":
						keepRule = val.depends.call(element, element);
						break;
				}
				if (keepRule) {
					rules[prop] = val.param !== undefined ? val.param : true;
				} else {
					delete rules[prop];
				}
			}
		});

		// evaluate parameters
		$.each(rules, function(rule, parameter) {
			rules[rule] = $.isFunction(parameter) ? parameter(element) : parameter;
		});

		// clean number parameters
		$.each(['minlength', 'maxlength', 'min', 'max'], function() {
			if (rules[this]) {
				rules[this] = Number(rules[this]);
			}
		});
		$.each(['rangelength', 'range'], function() {
			if (rules[this]) {
				rules[this] = [Number(rules[this][0]), Number(rules[this][1])];
			}
		});

		if ($.validator.autoCreateRanges) {
			// auto-create ranges
			if (rules.min && rules.max) {
				rules.range = [rules.min, rules.max];
				delete rules.min;
				delete rules.max;
			}
			if (rules.minlength && rules.maxlength) {
				rules.rangelength = [rules.minlength, rules.maxlength];
				delete rules.minlength;
				delete rules.maxlength;
			}
		}

		// To support custom messages in metadata ignore rule methods titled "messages"
		if (rules.messages) {
			delete rules.messages;
		}

		return rules;
	},

	// Converts a simple string to a {string: true} rule, e.g., "required" to {required:true}
	normalizeRule: function(data) {
		if( typeof data == "string" ) {
			var transformed = {};
			$.each(data.split(/\s/), function() {
				transformed[this] = true;
			});
			data = transformed;
		}
		return data;
	},

	// http://docs.jquery.com/Plugins/Validation/Validator/addMethod
	addMethod: function(name, method, message) {
		$.validator.methods[name] = method;
		$.validator.messages[name] = message != undefined ? message : $.validator.messages[name];
		if (method.length < 3) {
			$.validator.addClassRules(name, $.validator.normalizeRule(name));
		}
	},

	methods: {

		// http://docs.jquery.com/Plugins/Validation/Methods/required
		required: function(value, element, param) {
			// check if dependency is met
			if ( !this.depend(param, element) )
				return "dependency-mismatch";
			switch( element.nodeName.toLowerCase() ) {
			case 'select':
				// could be an array for select-multiple or a string, both are fine this way
				var val = $(element).val();
				return val && val.length > 0;
			case 'input':
				if ( this.checkable(element) )
					return this.getLength(value, element) > 0;
			default:
				return $.trim(value).length > 0;
			}
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/remote
		remote: function(value, element, param) {
			if ( this.optional(element) )
				return "dependency-mismatch";

			var previous = this.previousValue(element);
			if (!this.settings.messages[element.name] )
				this.settings.messages[element.name] = {};
			previous.originalMessage = this.settings.messages[element.name].remote;
			this.settings.messages[element.name].remote = previous.message;

			param = typeof param == "string" && {url:param} || param;

			if ( this.pending[element.name] ) {
				return "pending";
			}
			if ( previous.old === value ) {
				return previous.valid;
			}

			previous.old = value;
			var validator = this;
			this.startRequest(element);
			var data = {};
			data[element.name] = value;
			$.ajax($.extend(true, {
				url: param,
				mode: "abort",
				port: "validate" + element.name,
				dataType: "json",
				data: data,
				success: function(response) {
					validator.settings.messages[element.name].remote = previous.originalMessage;
					var valid = response === true;
					if ( valid ) {
						var submitted = validator.formSubmitted;
						validator.prepareElement(element);
						validator.formSubmitted = submitted;
						validator.successList.push(element);
						validator.showErrors();
					} else {
						var errors = {};
						var message = response || validator.defaultMessage( element, "remote" );
						errors[element.name] = previous.message = $.isFunction(message) ? message(value) : message;
						validator.showErrors(errors);
					}
					previous.valid = valid;
					validator.stopRequest(element, valid);
				}
			}, param));
			return "pending";
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/minlength
		minlength: function(value, element, param) {
			return this.optional(element) || this.getLength($.trim(value), element) >= param;
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/maxlength
		maxlength: function(value, element, param) {
			return this.optional(element) || this.getLength($.trim(value), element) <= param;
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/rangelength
		rangelength: function(value, element, param) {
			var length = this.getLength($.trim(value), element);
			return this.optional(element) || ( length >= param[0] && length <= param[1] );
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/min
		min: function( value, element, param ) {
			return this.optional(element) || value >= param;
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/max
		max: function( value, element, param ) {
			return this.optional(element) || value <= param;
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/range
		range: function( value, element, param ) {
			return this.optional(element) || ( value >= param[0] && value <= param[1] );
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/email
		email: function(value, element) {
			// contributed by Scott Gonzalez: http://projects.scottsplayground.com/email_address_validation/
			return this.optional(element) || /^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))$/i.test(value);
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/url
		url: function(value, element) {
			// contributed by Scott Gonzalez: http://projects.scottsplayground.com/iri/
			return this.optional(element) || /^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+\|,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+\|,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+\|,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+\|,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+\|,;=]|:|@)|\/|\?)*)?$/i.test(value);
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/date
		date: function(value, element) {
			return this.optional(element) || !/Invalid|NaN/.test(new Date(value));
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/dateISO
		dateISO: function(value, element) {
			return this.optional(element) || /^\d{4}[\/-]\d{1,2}[\/-]\d{1,2}$/.test(value);
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/number
		number: function(value, element) {
			return this.optional(element) || /^-?(?:\d+|\d{1,3}(?:,\d{3})+)(?:\.\d+)?$/.test(value);
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/digits
		digits: function(value, element) {
			return this.optional(element) || /^\d+$/.test(value);
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/creditcard
		// based on http://en.wikipedia.org/wiki/Luhn
		creditcard: function(value, element) {
			if ( this.optional(element) )
				return "dependency-mismatch";
			// accept only spaces, digits and dashes
			if (/[^0-9 -]+/.test(value))
				return false;
			var nCheck = 0,
				nDigit = 0,
				bEven = false;

			value = value.replace(/\D/g, "");

			for (var n = value.length - 1; n >= 0; n--) {
				var cDigit = value.charAt(n);
				var nDigit = parseInt(cDigit, 10);
				if (bEven) {
					if ((nDigit *= 2) > 9)
						nDigit -= 9;
				}
				nCheck += nDigit;
				bEven = !bEven;
			}

			return (nCheck % 10) == 0;
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/accept
		accept: function(value, element, param) {
			param = typeof param == "string" ? param.replace(/,/g, '|') : "png|jpe?g|gif";
			return this.optional(element) || value.match(new RegExp(".(" + param + ")$", "i"));
		},

		// http://docs.jquery.com/Plugins/Validation/Methods/equalTo
		equalTo: function(value, element, param) {
			// bind to the blur event of the target in order to revalidate whenever the target field is updated
			// TODO find a way to bind the event just once, avoiding the unbind-rebind overhead
			var target = $(param).unbind(".validate-equalTo").bind("blur.validate-equalTo", function() {
				$(element).valid();
			});
			return value == target.val();
		}

	}

});

// deprecated, use $.validator.format instead
$.format = $.validator.format;

})(jQuery);

// ajax mode: abort
// usage: $.ajax({ mode: "abort"[, port: "uniqueport"]});
// if mode:"abort" is used, the previous request on that port (port can be undefined) is aborted via XMLHttpRequest.abort()
;(function($) {
	var pendingRequests = {};
	// Use a prefilter if available (1.5+)
	if ( $.ajaxPrefilter ) {
		$.ajaxPrefilter(function(settings, _, xhr) {
			var port = settings.port;
			if (settings.mode == "abort") {
				if ( pendingRequests[port] ) {
					pendingRequests[port].abort();
				}
				pendingRequests[port] = xhr;
			}
		});
	} else {
		// Proxy ajax
		var ajax = $.ajax;
		$.ajax = function(settings) {
			var mode = ( "mode" in settings ? settings : $.ajaxSettings ).mode,
				port = ( "port" in settings ? settings : $.ajaxSettings ).port;
			if (mode == "abort") {
				if ( pendingRequests[port] ) {
					pendingRequests[port].abort();
				}
				return (pendingRequests[port] = ajax.apply(this, arguments));
			}
			return ajax.apply(this, arguments);
		};
	}
})(jQuery);

// provides cross-browser focusin and focusout events
// IE has native support, in other browsers, use event caputuring (neither bubbles)

// provides delegate(type: String, delegate: Selector, handler: Callback) plugin for easier event delegation
// handler is only called when $(event.target).is(delegate), in the scope of the jquery-object for event.target
;(function($) {
	// only implement if not provided by jQuery core (since 1.4)
	// TODO verify if jQuery 1.4's implementation is compatible with older jQuery special-event APIs
	if (!jQuery.event.special.focusin && !jQuery.event.special.focusout && document.addEventListener) {
		$.each({
			focus: 'focusin',
			blur: 'focusout'
		}, function( original, fix ){
			$.event.special[fix] = {
				setup:function() {
					this.addEventListener( original, handler, true );
				},
				teardown:function() {
					this.removeEventListener( original, handler, true );
				},
				handler: function(e) {
					arguments[0] = $.event.fix(e);
					arguments[0].type = fix;
					return $.event.handle.apply(this, arguments);
				}
			};
			function handler(e) {
				e = $.event.fix(e);
				e.type = fix;
				return $.event.handle.call(this, e);
			}
		});
	};
	$.extend($.fn, {
		validateDelegate: function(delegate, type, handler) {
			return this.bind(type, function(event) {
				var target = $(event.target);
				if (target.is(delegate)) {
					return handler.apply(target, arguments);
				}
			});
		}
	});
})(jQuery);
