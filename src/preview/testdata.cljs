(ns preview.testdata
  (:require ["fs" :as fs]
            [ewr.serialization :as serialize]
            ["nodejs-base64-converter" :as nodeBase64]))


(def teststate
  {:energy-needed 3000
   :energy-sources
   {:wind
    {:share             28
     :power-density     4.56
     :deaths            0.12
     :co2               11
     :resources         10260
     :arealess-capacity 240}
    :solar
    {:share             12
     :power-density     5.2
     :deaths            0.44
     :co2               44
     :resources         16447
     :arealess-capacity 142}
    :nuclear
    {:share         15
     :power-density 240.8
     :deaths        0.08
     :co2           12
     :resources     930}
    :bio
    {:share         2
     :power-density 0.16
     :deaths        4.63
     :co2           230
     :resources     1080}
    :natural-gas
    {:share         12
     :power-density 482.1
     :deaths        2.82
     :co2           490
     :resources     572}
    :coal
    {:share         31
     :power-density 135.1
     :deaths        28.67
     :co2           820
     :resources     1185}}})

;; (def teststate-string
;;   "AX9AIarI0vJik08J2F30DFHMXlVqrSiq7SsPoEdheWvO1MVwW-hF5KVZQVgXTtvoGKrRJsS0VXOjdh9ARpEOTYaUdrR0lhve8")

(def teststate-string
  (serialize/encode-savestate-huff
   (str
    (serialize/serialize teststate))))

"AX9AD9WRpeTFJp4TsLvoGKOYvKrVWlFV2lYfQI7C8tedqYrgt9CLyUqygrAunbfQMVWiTYloqudG7D6AjSIcmw0o7WjpLDe94"

(def test-request
  (clj->js
   {"version"               "2.0"
    "routeKey"              "GET /preview"
    "rawPath"               "/test/preview"
    "rawQueryString"        "savestate=AX9AD9WRpeTFJp4TsLvoGKOYvKrVWlFV2lYfQI7C8tedqYrgt9CLyUqygrAunbfQMVWiTYloqudG7D6AjSIcmw0o7WjpLDe94"
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
    "queryStringParameters" {"savestate" teststate-string}
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

(defn dummy-callback
  [_ response]
  (let [png-buffer (.decode nodeBase64 (get (js->clj response) "body"))]
    ;; (print "png-buffer is " png-buffer)
    (.writeFileSync fs "test.png" png-buffer
                    )))