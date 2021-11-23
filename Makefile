.PHONY: dev-setup
dev-setup:
	npm install
	mkdir -p export/main >/dev/null
	mkdir -p export/preview >/dev/null
	mkdir -p export/other >/dev/null
	npx shadow-cljs release prerender

.PHONY: export-main-project
export-main-project:
	rm -r .shadow-cljs
	npx shadow-cljs release prerender
	lein build-site
	npx shadow-cljs release app
	chmod -R 755 export/main

.PHONY: prod-export-main-project
prod-export-main-project:
	EWR_CONFIG_DIRS="config/default" make export-main-project

.PHONY: stage-export-main-project
stage-export-main-project:
	EWR_CONFIG_DIRS="config/default config/default_stage" make export-main-project

.PHONY: build-aws-preview
build-aws-preview:
	rm -r .shadow-cljs
	rm -rf export/preview/*
	cp -r resources/preview/* export/preview/
	npx shadow-cljs release aws-preview
	cp -r node_modules export/preview/node_modules
	#These are unneeded for AWS and removed to keep the zip small
	rm -r export/preview/node_modules/puppeteer
	rm -r export/preview/node_modules/shadow-cljs-jar
	cd export/preview/ && zip -r lambda.zip node_modules index.js fonts
