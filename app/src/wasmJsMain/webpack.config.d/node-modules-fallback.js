// Webpack 5 rejects the `node:` URI scheme by default (it only handles `data:` and `file:`).
// Some transitive JS dependencies of our wasmJs build reference Node.js built-ins via that
// scheme — notably @js-joda/core (pulled in by kotlinx-datetime) imports `node:net`, and
// ktor-client-js may touch other `node:*` modules. Those code paths only run under Node.js
// (the libraries feature-detect the environment and skip them in a browser), so we can safely
// short-circuit every `node:*` request to an empty module here.
//
// Without this, `:app:wasmJsBrowserDevelopmentWebpack` fails with:
//   Module build failed: UnhandledSchemeError: Reading from "node:net" is not handled by plugins
//
// Approach: register a resolver hook (runs before webpack's built-in scheme handler) that
// intercepts `node:*` requests and rewrites them to an in-memory empty module.
;(function (config) {
    var webpack = require("webpack");
    config.resolve = config.resolve || {};
    config.resolve.plugins = config.resolve.plugins || [];

    // NormalModuleReplacementPlugin runs during the "before-relative" resolve step and lets us
    // rewrite the request *before* webpack's scheme validation rejects it. We point every
    // `node:*` import at a generated empty module on disk.
    var path = require("path");
    var fs = require("fs");
    var emptyDir = path.join(__dirname, "kotlin", "_node-empty");
    try { fs.mkdirSync(emptyDir, { recursive: true }); } catch (e) { /* already exists */ }
    var emptyFile = path.join(emptyDir, "empty.js");
    try { fs.writeFileSync(emptyFile, "module.exports = {};\n"); } catch (e) { /* already exists */ }

    config.plugins = config.plugins || [];
    config.plugins.push(new webpack.NormalModuleReplacementPlugin(
        /^node:/,
        function (resource) {
            resource.request = emptyFile;
        },
    ));
})(config);
