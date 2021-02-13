(ns stromrechner.db)

(def default-db
  {:energy-sources {:wind {:name "Wind"
                           :share 40
                           :locked? false
                           :props {:cx 400
                                   :cy 250
                                   :fill "rgba(135, 206, 250,0.6)" ; lightskyblue
                                   }} 
                    :solar {:name "Sonne"
                            :share 40
                            :locked? false
                            :props {:cx 350
                                    :cy 700
                                    :fill "rgba(255, 255, 0, 0.6)"}}
                    :nuclear {:name "Kernenergie"
                              :share 5
                              :locked? false
                              :props {:cx 130
                                      :cy 450
                                      :fill "rgba(250, 147, 38, 0.5)"}}
                    :bio {:name "Biogas"
                          :share 15
                          :locked? false
                          :props {:cx 320
                                  :cy 450
                                  :fill "rgba(50, 205, 50, 0.5)"}}}})


