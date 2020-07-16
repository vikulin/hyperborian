function httpGet(theUrl, text, file, lang)
{
	var XHR = ("onload" in new XMLHttpRequest()) ? XMLHttpRequest : XDomainRequest;
	var xhr = new XHR();
	xhr.open('GET', theUrl, true);

	xhr.onload = function() {
  	  var html = JSON.stringify(JSON.parse(this.responseText).parse.text['*']);
  	  var htmlObjects = $.parseHTML(html)[1];
  	  var xPathText = $(htmlObjects).find('table[class*="infobox"] ~ p');
  	  var xPathA = $(htmlObjects).find('table[class*="infobox"] a > *');
	  html='';
  	  var arrayLength = xPathText.length;
  	  for(var i = 0; i < arrayLength; i++){
  		 var onlyText = $(xPathText[i]); 
  		 onlyText.find('sup').remove();
  		 onlyText.find('a').each(function(j, obj) {
  			var href = obj.getAttribute('href').replace(/\\"/g, '');
  			obj.setAttribute('href', 'https://'+lang+'.wikipedia.org'+href);
  			obj.setAttribute('class', 'z-a');
  		 });
  		 onlyText.find('span[class*="ts-"]').remove();
  		 html+=onlyText.html().replace(/\\n/g, '\n');
  	  }
  	  arrayLength = xPathA.length;
	  text.innerHTML = html;
	  //get first a: this is an image
	  var a = xPathA[0];
	  var imgTag = a.outerHTML;
	  var image = $.parseHTML(imgTag)[0];
	  	  
	  if(a.hasAttribute('width')){
		  var width = parseInt(a.getAttribute('width').replace(/\\"/g, ''));
		  if(width<25){
			  a.remove();
		  } else {
			  var src = image.getAttribute('src').replace(/\\"/g, '');
			  if(image.hasAttribute('srcset')) {
				  var srcset = image.getAttribute('srcset').replace(/\\"/g, '');
				  a.setAttribute('srcset', srcset);
			  }
			  var decoding = image.getAttribute('decoding').replace(/\\"/g, '');
			  a.setAttribute('src', src);
			  a.setAttribute('width', '171');
			  a.setAttribute('decoding', decoding);
			  file.innerHTML = a.outerHTML;
		  }
	  }
	}

	xhr.onerror = function() {
	  alert( 'Ошибка ' + this.status );
	}

	xhr.send()
}

var files = document.getElementsByClassName('z-a wiki_title');
var text = document.getElementsByClassName('wiki_text');
for (i = 0; i < files.length; i++) {
	var wikiTitle = files[i].innerHTML;
	var lang = files[i].parentElement.getAttribute("lang");
	var url = 'https://'+lang+'.wikipedia.org/w/api.php?action=parse&page='+encodeURIComponent(wikiTitle)+'&prop=text&section=0&format=json&origin=*';
	var imgTag = httpGet(url, text[i], files[i], lang);
}

