(ns stromrechner.macros)

(defmacro def-from-file [var file f]
  `(def ~var
     (~f ~(read-string (slurp file)))))

(defmacro def-string-from-file [var file f]
  `(def ~var
     (~f ~(slurp file))))
