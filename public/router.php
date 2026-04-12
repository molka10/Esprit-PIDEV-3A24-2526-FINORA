<?php

/**
 * Routeur pour le serveur PHP intégré : sert les fichiers statiques s'ils existent,
 * sinon délègue à Symfony (nécessaire pour /assets/*.js, importmap, etc.).
 *
 * Démarrage (depuis finora_web) :
 *   php -S 127.0.0.1:8000 -t public public/router.php
 *
 * Ne pas utiliser asset-map:compile en local : sinon manifest.json fige des hash
 * différents de ceux générés par le serveur de dev → 404.
 */
$path = parse_url($_SERVER['REQUEST_URI'] ?? '', PHP_URL_PATH);
if (\is_string($path) && $path !== '' && $path !== '/') {
    $path = rawurldecode($path);
    $file = __DIR__.str_replace('/', \DIRECTORY_SEPARATOR, $path);
    if (is_file($file)) {
        return false;
    }
}

$_SERVER['SCRIPT_FILENAME'] = __DIR__.'/index.php';

require dirname(__DIR__).'/vendor/autoload_runtime.php';
