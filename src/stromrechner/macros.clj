(ns stromrechner.macros)

(defmacro def-from-file [var file]
  `(def ~var
    ~(read-string (slurp file))))
