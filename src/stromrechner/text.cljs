(ns stromrechner.text
  (:require-macros [stromrechner.macros :as m]))

(def snippets 
  (m/build-text-map
   {:general {:heading "Allgemeines"}
    :power-density {:heading "Durchschnittsleistung"}
    :deaths {:heading "Statistisch erwartbare Todesfälle pro TWh"}
    :co2 {:heading [:span "CO" [:sub "2"] "-Äquivalent" ]}
    :wind {:heading "Windenergie"}
    :solar {:heading "Photovoltaik"}
    :nuclear {:heading "Kernenergie"}
    :bio {:heading "Biomasse"}
    :natural-gas {:heading "Erdgas"}
    :coal {:heading "Kohle"}
    }))




 
 
