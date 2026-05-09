(function () {
  function load(placeholder) {
    var id = placeholder.getAttribute('data-id');
    if (!id) return;
    var title = placeholder.getAttribute('data-title') || 'YouTube video';
    var iframe = document.createElement('iframe');
    iframe.setAttribute('frameborder', '0');
    iframe.setAttribute('allow', 'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture');
    iframe.setAttribute('allowfullscreen', '');
    iframe.title = title;
    iframe.src = 'https://www.youtube-nocookie.com/embed/' + encodeURIComponent(id) + '?autoplay=1&playsinline=1&rel=0';
    placeholder.replaceWith(iframe);
  }

  function init() {
    var buttons = document.querySelectorAll('.yt-embed__btn');
    for (var i = 0; i < buttons.length; i++) {
      buttons[i].addEventListener('click', function (event) {
        event.preventDefault();
        var placeholder = this.closest('.yt-embed');
        if (placeholder) load(placeholder);
      });
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
