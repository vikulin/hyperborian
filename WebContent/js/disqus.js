/**
					*  RECOMMENDED CONFIGURATION VARIABLES: EDIT AND UNCOMMENT THE SECTION BELOW TO INSERT DYNAMIC VALUES FROM YOUR PLATFORM OR CMS.
					*  LEARN WHY DEFINING THESE VARIABLES IS IMPORTANT: https://disqus.com/admin/universalcode/#configuration-variables*/
		
		var disqus_config = function () {
		this.page.url = window.location.href;
		this.page.identifier = getJsonFromUrl(window.location.href)['id'] 
		};
		
		function getJsonFromUrl(url) {
		  if(!url) url = location.search;
		  var query = url.split("?")[1];
		  var result = {};
		  query.split("&").forEach(function(part) {
		    var item = part.split("=");
		    result[item[0]] = decodeURIComponent(item[1]);
		  });
		  return result;
		};
		
		(function() { // DON'T EDIT BELOW THIS LINE
			var d = document, s = d.createElement('script');
			s.src = 'https://hyperborian-1.disqus.com/embed.js';
			s.setAttribute('data-timestamp', +new Date());
			(d.head || d.body).appendChild(s);
			})();