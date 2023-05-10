.PHONY: dev-setup
dev-setup:
	npm install
	mkdir -p export/main >/dev/null
	mkdir -p export/preview >/dev/null
	mkdir -p export/other >/dev/null
	npx shadow-cljs release prerender
	lein build-site
	npx scss --update --force scss:export/main/css

.PHONY: export-main-project
export-main-project:
	rm -rf export 2> /dev/null || true
	mkdir export
	rm -rf .shadow-cljs 2> /dev/null || true
	echo "CLEAN SLATE – Export dir emptied"
	npx shadow-cljs release prerender
	npx scss  --style compressed --update --force scss:export/main/css
	npx webpack --mode production --config webpack/config.js
	npx shadow-cljs release app
	clj -X:build-site
	chmod -R 755 export/main

.PHONY: prepare-dev
prepare-dev:
	lein build-site
	npx scss --update --force scss:export/main/css
	npx webpack --mode development --config webpack/config.js

.PHONY: prod-export-main-project
prod-export-main-project:
	EWR_CONFIG_DIRS="config/default" make export-main-project

.PHONY: stage-export-main-project
stage-export-main-project:
	EWR_CONFIG_DIRS="config/default config/default_stage" make export-main-project

.PHONY: test-export-main-project
test-export-main-project:
	EWR_CONFIG_DIRS="config/default config/default_test" make export-main-project

.PHONY: build-aws-preview
build-aws-preview:
	rm -r .shadow-cljs 2> /dev/null || true
	rm -rf export/preview/* 2> /dev/null || true
	cp -r resources/preview/* export/preview/
	npx shadow-cljs release aws-preview
	cp -r node_modules export/preview/node_modules
	#Shadow-cljs is not needed for AWS and removed to keep the zip small
	rm -r export/preview/node_modules/shadow-cljs-jar
	cd export/preview/ && zip -r lambda.zip node_modules index.js fonts

## #######################
## ##### Development #####
## #######################

.PHONY: dev-watch-site
dev-watch-site:
	clj -X:watch-site

.PHONY: dev-watch-cljs
dev-watch-cljs:
	npx shadow-cljs watch app

.PHONY: dev-watch-scss
dev-watch-scss:
	npx scss --watch scss:export/main/css

.PHONY: dev-watch-webpack
dev-watch-webpack:
	npx webpack watch --mode development --config webpack/config.js

.PHONY: dev-watch-non-cljs
dev-watch-non-cljs:
	 make dev-watch-scss & make dev-watch-site  & make dev-watch-webpack

.PHONY: dev-run-php
dev-run-php:
	cd export/main && php -S localhost:5002
