# stromrechner

A [re-frame](https://github.com/day8/re-frame) application demonstrating the spatial requirements for various energy sources in relation to the size of Germany. The various parameters can be changed within the app and loaded from predefined scientific resources.

## Compilation

The project uses [shadow-cljs](https://github.com/thheller/shadow-cljs) for compilation. To install globally using NPM just type
```
npm install -g shadow-cljs
```
To create a release build type the following inside the project directory:
```
shadow-cljs release app
```
A development build is created with the following command:
```
shadow-cljs compile app
```

The compiled Javascript file can be found under `resources/public/js/compiled/`. To see the app in action, just open `resources/public/index.html` or serve the the directory. A development build will include [re-frame-10x](https://github.com/day8/re-frame-10x), a debugging dashboard to inspect the application state during execution (toggle with `CTRL-H`).

## Configuration

Without any knowledge in Clojure(Script) the app can be configured via the [EDN](https://github.com/edn-format/edn) files contained in the `config` directory. Scientific resources added to `publications.edn`  will be available in the detailed settings within the app (“Detaillierte Einstellungen”).

## Testing

There are a couple of unit tests that can be run inside node.js via `shadow-cljs compile node-test && node out/node-tests.js`.
