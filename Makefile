.PHONY: dev-setup
dev-setup:
	npm install
	mkdir -p export/main >/dev/null
	mkdir -p export/preview >/dev/null
	mkdir -p export/other >/dev/null
	npx shadow-cljs release prerender

.PHONY: prod-build-main-project
prod-build-main-project:
	npx shadow-cljs release prerender
	lein build-site-without-php
	npx shadow-cljs release app --config-merge "{:closure-defines {:disabled-features \"bookmark-state\"}}"
	chmmod -R 755 export/main

.PHONY: test-build-project
test-build-main-project:
	npx shadow-cljs release prerender
	lein build-site-with-php
	npx shadow-cljs release app --config-merge
	chmod -R 755 export/main

.PHONY: build-aws-preview
build-aws-preview:
	rm -rf export/preview/*
	cp -r resources/preview/* export/preview/
	npx shadow-cljs release aws-preview
	cp -r node_modules export/preview/node_modules
	#These are unneeded for AWS and removed to keep the zip small
	rm -r export/preview/node_modules/puppeteer
	rm -r export/preview/node_modules/shadow-cljs-jar
	cd export/preview/ && zip -r lambda.zip node_modules index.js fonts
