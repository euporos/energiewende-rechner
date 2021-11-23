(ns preview.main-test
  (:require [preview.main :as preview]
            ["fs" :as fs]
            [cljs.test :as t :include-macros true]
            ["nodejs-base64-converter" :as nodeBase64]))

(def testsvg "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>
<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">
<svg width=\"632pt\" height=\"91pt\" viewBox=\"0.00 0.00 631.61 91.30\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">
  <g id=\"graph0\" class=\"graph\" transform=\"scale(1 1) rotate(0) translate(4 112)\">
    <polygon fill=\"#ffffff\" stroke=\"transparent\" points=\"-4,4 -4,-112 58,-112 58,4 -4,4\"/>
    <ellipse fill=\"none\" stroke=\"#000000\" cx=\"27\" cy=\"-90\" rx=\"27\" ry=\"18\"/>
    <text text-anchor=\"middle\" x=\"27\" y=\"-85.8\" font-family=\"Times,serif\" font-size=\"14.00\" fill=\"#000000\">a</text>
  </g>
</svg>")

(def teststate-string
  "AX9AIarI0vJik08J2F30DFHMXlVqrSiq7SsPoEdheWvO1MVwW-hF5KVZQVgXTtvoGKrRJsS0VXOjdh9ARpEOTYaUdrR0lhve8")

(def test-request
  (clj->js
   {"version"               "2.0"
    "routeKey"              "GET /preview"
    "rawPath"               "/test/preview"
    "rawQueryString"        "savestate=Ai-gHVkaXkxSaeE7C76HRzF5Vaq0oqu0rD6B-wvLXnamK4LfQ7yUqygrAunbfQ6rRJsS0VXOjdh9DpEOTYaUdrR0lhve8"
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
    "queryStringParameters" {"savestate" teststate-string ;; "Ai-gHVkaXkxSaeE7C76HRzF5Vaq0oqu0rD6B-wvLXnamK4LfQ7yUqygrAunbfQ6rRJsS0VXOjdh9DpEOTYaUdrR0lhve8"
                             }
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
    (print "png-buffer is " png-buffer)
    (.writeFileSync fs "test.png" png-buffer)))

(defn ^:dev/after-load test-img-output
  []
  (preview/handler test-request nil dummy-callback))
