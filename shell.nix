{ pkgs ? import (fetchTarball "https://github.com/NixOS/nixpkgs/archive/6d02a514db95d3179f001a5a204595f17b89cb32.tar.gz") { } }:

pkgs.mkShell {
  buildInputs = [
    pkgs.nodejs-14_x
    pkgs.clojure
    pkgs.jdk11
    pkgs.nodePackages.npm
    pkgs.php
    pkgs.sass
  ];
}
