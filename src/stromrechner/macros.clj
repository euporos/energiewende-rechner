(ns stromrechner.macros)

(defmacro def-from-file [var file f]
  `(def ~var
     (~f ~(read-string (slurp file)))))
