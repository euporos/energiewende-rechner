(ns stromrechner.db)

(def default-db
  {:energy-sources {:wind {:share 40
                           :locked? false} 
                    :solar {:share 40
                            :locked? false}
                    :nuclear {:share 5
                              :locked? false}
                    :bio {:share 15
                          :locked? false}}})


