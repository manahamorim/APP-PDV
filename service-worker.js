// Service worker mínimo — existe basicamente para o Chrome considerar o
// site "instalável" como PWA (ícone na tela inicial, tela cheia, sem barra
// de navegador). Faz um cache simples do "shell" do app e deixa passar
// direto (sem cache) qualquer chamada à API do Apps Script.

var CACHE_NAME = 'positivacao-shell-v1';
var SHELL_FILES = ['./', './index.html', './manifest.json', './icon-192.png', './icon-512.png'];

self.addEventListener('install', function (event) {
  event.waitUntil(
    caches.open(CACHE_NAME).then(function (cache) {
      return cache.addAll(SHELL_FILES);
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', function (event) {
  event.waitUntil(
    caches.keys().then(function (keys) {
      return Promise.all(
        keys.filter(function (k) { return k !== CACHE_NAME; }).map(function (k) { return caches.delete(k); })
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', function (event) {
  var url = new URL(event.request.url);

  // Nunca interceptar chamadas à API (Apps Script) — sempre ir direto à rede.
  if (url.origin !== self.location.origin) {
    return;
  }

  // Para arquivos do próprio app: cache primeiro, com atualização em segundo
  // plano; se não houver rede nem cache, cai no index.html.
  event.respondWith(
    caches.match(event.request).then(function (cached) {
      var networkFetch = fetch(event.request)
        .then(function (response) {
          if (response && response.status === 200) {
            var copy = response.clone();
            caches.open(CACHE_NAME).then(function (cache) { cache.put(event.request, copy); });
          }
          return response;
        })
        .catch(function () { return cached || caches.match('./index.html'); });
      return cached || networkFetch;
    })
  );
});
