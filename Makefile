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
	rm -r .shadow-cljs
	rm -r export/*
	echo "CLEAN SLATE – Export dir emptied"
	npx shadow-cljs release prerender
	npx scss  --style compressed --update --force scss:export/main/css
	npx shadow-cljs release app
	lein build-site
	chmod -R 755 export/main

.PHONY: prepare-dev
prepare-dev:
	lein build-site
	npx scss --update --force scss:export/main/css

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
	rm -r .shadow-cljs
	rm -rf export/preview/*
	cp -r resources/preview/* export/preview/
	npx shadow-cljs release aws-preview
	cp -r node_modules export/preview/node_modules
	#Shadow-cljs is unneeded for AWS and removed to keep the zip small
	rm -r export/preview/node_modules/shadow-cljs-jar
	cd export/preview/ && zip -r lambda.zip node_modules index.js fonts

## #######################
## ##### Development #####
## #######################

.PHONY: dev-watch-site
dev-watch-site:
	lein auto build-site

.PHONY: dev-watch-cljs
dev-watch-cljs:
	npx shadow-cljs watch app

.PHONY: dev-watch-scss
dev-watch-scss:
	npx scss --watch scss:export/main/css

.PHONY: dev-watch-all
dev-watch-all:
	 make dev-watch-scss & make dev-watch-site & make dev-watch-cljs

.PHONY: dev-run-php
dev-run-php:
	cd export/main && php -S localhost:5002
