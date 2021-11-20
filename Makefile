.PHONY: dev-setup
dev-setup:
	npm install
	mkdir -p export/main >/dev/null
	mkdir -p export/preview >/dev/null
	mkdir -p export/other >/dev/null
	npx release prerender

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
