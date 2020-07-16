function fireOnScroll(event) {
  var scrollableHeight = this.scrollHeight - this.clientHeight;
  var scrollableWidth = this.scrollWidth - this.clientWidth;
  
  var scrollPercentageX = scrollableWidth > 0 ? this.scrollLeft / scrollableWidth * 100 : 100;
  var scrollPercentageY = scrollableHeight > 0 ? this.scrollTop / scrollableHeight * 100 : 100;
  
  var data = {
    scrollLeft: this.scrollLeft,
    scrollTop: this.scrollTop,
    scrollHeight: this.scrollHeight,
    scrollWidth: this.scrollWidth,
    clientHeight: this.clientHeight,
    clientWidth: this.clientWidth,
    scrollPercentageX: scrollPercentageX,
    scrollPercentageY: scrollPercentageY
  };
  zk.$(this).fire('onScroll', data, null, 50); //some delay to avoid too many events (caused by smooth scrolling)
}