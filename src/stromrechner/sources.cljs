(ns stromrechner.sources)

;; Units

;; Power-density: W/m²
;; capacity-facor: 1=100%
;; Deaths: per TWh


(def publications
  [{:id "Quaschning (2016) – 2050 optimistisch"
    :link "https://www.volker-quaschning.de/publis/studien/sektorkopplung/Sektorkopplungsstudie.pdf"
    :energy-needed 1300}
   {:id "Quaschning (2016) – 2050 pessimistisch"
    :link "https://www.volker-quaschning.de/publis/studien/sektorkopplung/Sektorkopplungsstudie.pdf"
    :energy-needed 3000}
   {:id "Forbes"
    :link "https://web.archive.org/web/20150724024259/http://nextbigfuture.com/2011/03/deaths-per-twh-by-energy-source.html"
    :energy-sources {:bio {:deaths 12}
                 :solar {:deaths 0.44}
                 :wind {:deaths 0.12}
                 :nuclear {:deaths 0.09}}}
   {:id "Conservation Biology"
    :link "https://doi.org/10.1111/cobi.12433"
    :energy-sources {:bio {:deaths 4}
                 :solar {:deaths 0.44}
                 :wind {:deaths 0.15}
                 :nuclear {:deaths 0.04}}}
   {:id "Energy Policy (Metastudie)"
    :link "https://doi.org/10.1016/j.enpol.2018.08.023"
    :energy-sources {:bio {:power-density 0.85
                          :capacity-factor 0.53}
                     :solar {:power-density 6.32
                         :capacity-factor 1}
                     :wind {:power-density 6.37
                        :capacity-factor 0.3}
                     :nuclear {:power-density 259
                           :capacity-factor 0.93}}}
   {:id "BMVI, Berlin, 2015"
    :link "https://www.bbsr.bund.de/BBSR/DE/veroeffentlichungen/ministerien/bmvi/bmvi-online/2015/BMVI_Online_08_15.html"
    :energy-sources {:wind {:power-density 20
                        :capacity-factor 0.23}
                 :solar {:power-density 44.44
                         :capacity-factor 0.11}
                 :bio {:power-density 0.2  ;; brauch ich noch Hilfe
                       :capacity-factor 0.3}}}
   {:id "Deutscher Bundestag, 2007"
    :link "https://www.bundestag.de/resource/blob/406432/70f77c4c170d9048d88dcc3071b7721c/wd-8-056-07-pdf-data.pdf"
    :energy-sources {:wind {:co2 24000} ; per t/TWh
                     :solar {:co2 101000}
                     :nuclear {:co2 19000}
                     :bio {:co2 0}}}])


(defn pubs-for-param
  ""
  [nrg-key param-key]
  (filter
   #(get-in % [:energy-sources nrg-key param-key])   
   publications))

(defn matching-pubs
  ""
  [nrg-key param-key value]
  (filter
   #(= (get-in % [:energy-sources nrg-key param-key]) value)
   publications))

(defn matching-pubs-for-path
  ""
  [path value]
  (filter
   #(= (get-in % path) value)
   publications))

(defn pubs-for-needed-power
  "Returns all publications providing a value
  for :needed Power"
  []
  (filter
   #(get % :energy-needed)   
   publications))





(defn reverse-paths
  ""
  [indata]
  (let [first-level-keys (keys indata)
        second-level-keys (keys (reduce merge (map second indata)))
        paths (for [flk first-level-keys
                    slk second-level-keys]
                [flk slk])]
    
    (reduce
     (fn [sofar nextpath]
       (assoc-in sofar (vec (reverse nextpath))
                 (get-in indata nextpath)))
     {} paths)))


(defn default-pub
  ""
  [nrg-key param-key]
  (some #(if (get-in % [:energy-sources nrg-key param-key]) %)
        publications))




(reverse-paths
 {:flaechenverbrauch {:bio 120, :wind 40, :solar 140, :kern 0.1},
  :vollast {:bio 0.8, :solar 0.33, :wind 0.45, :kern 0.85},
  :deaths {:bio 2, :solar 0.4, :wind 0.1, :kern 0.9}})






  
 
