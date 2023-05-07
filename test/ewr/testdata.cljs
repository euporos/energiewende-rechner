(ns ewr.testdata
  (:require [ewr.config :as cfg]
            [ewr.serialization :as serialize]))

(def preset-1
  {:energy-sources
   {:wind
    {:share 604000
     :power-density 4.56
     :deaths 0.12
     :co2 11
     :resources 10260
     :arealess-capacity 240}
    :solar
    {:share 259000
     :power-density 5.2
     :deaths 0.44
     :co2 44
     :resources 16447
     :arealess-capacity 142}
    :bio
    {:share 43000
     :power-density 0.16
     :deaths 4.63
     :co2 230
     :resources 1080}
    :nuclear
    {:share 324000
     :power-density 240.8
     :deaths 0.08
     :co2 12
     :resources 930}
    :natural-gas
    {:share 259000
     :power-density 482.1
     :deaths 2.82
     :co2 490
     :resources 572}
    :coal
    {:share 648000
     :power-density 135.1
     :deaths 28.67
     :co2 820
     :resources 1185}
    :hydro
    {:cap 42000
     :share 22000
     :power-density 2.28
     :deaths 0.14
     :co2 24
     :resources 14068}}})

(def preset-2
  {:energy-sources
   {:wind
    {:share 604000
     :cap 604000
     :power-density 4.56
     :deaths 0.12
     :co2 11
     :resources 10260
     :arealess-capacity 240}
    :solar
    {:share 259000
     :power-density 5.2
     :deaths 0.44
     :co2 44
     :resources 16447
     :arealess-capacity 142}
    :bio
    {:share 43000
     :power-density 0.16
     :deaths 4.63
     :co2 230
     :resources 1080}
    :nuclear
    {:share 324000
     :power-density 240.8
     :deaths 0.08
     :co2 12
     :resources 930}
    :natural-gas
    {:share 259000
     :power-density 482.1
     :deaths 2.82
     :co2 490
     :resources 572}
    :coal
    {:share 648000
     :power-density 135.1
     :deaths 28.67
     :co2 820
     :resources 1185}
    :hydro
    {:cap 42000
     :share 22000
     :power-density 2.28
     :deaths 0.14
     :co2 24
     :resources 14068}}})

(def state-1
  {:energy-sources
   {:wind
    {:share 704000 ;; differs +100000
     }
    :solar
    {:share 249000} ;; differs -100000
    :bio
    {:share 43000}
    :nuclear
    {:share 324000}
    :natural-gas
    {:share 259000}
    :coal
    {:share 648000}
    :hydro
    {:share 22000}}})

(def state-2
  {:energy-sources
   {:wind
    {:share 0 ;; differs +100000
     }
    :solar
    {:share 0} ;; differs -100000
    :bio
    {:share 0}
    :nuclear
    {:share 2159000}
    :natural-gas
    {:share 0}
    :coal
    {:share 0}
    :hydro
    {:share 0}}})

(def teststate-string
  (serialize/savestate->string state-2))

(def test-request
  (clj->js
   {"version"               "2.0"
    "routeKey"              "GET /preview"
    "rawPath"               "/test/preview"
    "rawQueryString"        (str "s=" teststate-string)
    "headers"               {"sec-fetch-site"            "none"
                             "host"                      "9xcjcr4b4m.execute-api.eu-central-1.amazonaws.com"
                             "user-agent"                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"
                             "content-length"            "0"
                             "sec-fetch-user"            "?1"
                             "x-forwarded-port"          "443"
                             "upgrade-insecure-requests" "1"
                             "accept"                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
                             "accept-language"           "en-US,en;q=0.9,de;q=0.8"
                             "sec-fetch-dest"            "document"
                             "x-amzn-trace-id"           "Root=1-6147334f-4a31d70c64579e445c79f104"
                             "x-forwarded-for"           "78.35.254.29"
                             "accept-encoding"           "gzip, deflate, br"
                             "x-forwarded-proto"         "https"
                             "sec-fetch-mode"            "navigate"
                             "dnt"                       "1"
                             "sec-gpc"                   "1"
                             "cache-control"             "max-age=0"}
    "queryStringParameters" {"s" teststate-string}
    "requestContext"        {"accountId"    "012791859856"
                             "routeKey"     "GET /preview"
                             "domainName"   "9xcjcr4b4m.execute-api.eu-central-1.amazonaws.com"
                             "http"         {"method"    "GET"
                                             "path"      "/test/preview"
                                             "protocol"  "HTTP/1.1"
                                             "sourceIp"  "78.35.254.29"
                                             "userAgent" "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"}
                             "timeEpoch"    1632056143992, "requestId" "F6T0hhDQFiAEJHg="
                             "apiId"        "9xcjcr4b4m"
                             "time"         "19/Sep/2021:12:55:43 +0000"
                             "domainPrefix" "9xcjcr4b4m"
                             "stage"        "test"}
    "isBase64Encoded"       false}))

(def empty-request
  (clj->js
   {"version"               "2.0"
    "routeKey"              "GET /preview"
    "rawPath"               "/test/preview"
    "rawQueryString"        ""
    "headers"               {"sec-fetch-site"            "none"
                             "host"                      "9xcjcr4b4m.execute-api.eu-central-1.amazonaws.com"
                             "user-agent"                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"
                             "content-length"            "0"
                             "sec-fetch-user"            "?1"
                             "x-forwarded-port"          "443"
                             "upgrade-insecure-requests" "1"
                             "accept"                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
                             "accept-language"           "en-US,en;q=0.9,de;q=0.8"
                             "sec-fetch-dest"            "document"
                             "x-amzn-trace-id"           "Root=1-6147334f-4a31d70c64579e445c79f104"
                             "x-forwarded-for"           "78.35.254.29"
                             "accept-encoding"           "gzip, deflate, br"
                             "x-forwarded-proto"         "https"
                             "sec-fetch-mode"            "navigate"
                             "dnt"                       "1"
                             "sec-gpc"                   "1"
                             "cache-control"             "max-age=0"}
    "queryStringParameters" {}
    "requestContext"        {"accountId"    "012791859856"
                             "routeKey"     "GET /preview"
                             "domainName"   "9xcjcr4b4m.execute-api.eu-central-1.amazonaws.com"
                             "http"         {"method"    "GET"
                                             "path"      "/test/preview"
                                             "protocol"  "HTTP/1.1"
                                             "sourceIp"  "78.35.254.29"
                                             "userAgent" "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"}
                             "timeEpoch"    1632056143992, "requestId" "F6T0hhDQFiAEJHg="
                             "apiId"        "9xcjcr4b4m"
                             "time"         "19/Sep/2021:12:55:43 +0000"
                             "domainPrefix" "9xcjcr4b4m"
                             "stage"        "test"}
    "isBase64Encoded"       false}))

