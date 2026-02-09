{
  description = "Dev shell: Java (JDK 21, Maven, Gradle)";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-25.11";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
      in {
        devShells.default = pkgs.mkShell {
          name = "java-devshell";

          packages = with pkgs; [
            jdk21
            maven
            gradle
            jdt-language-server
            # JavaFX Dependencies
            gtk3
            libGL
            glib
            pango
            cairo
            atk
            gdk-pixbuf
            fontconfig
            freetype
            xorg.libX11
            xorg.libXext
            xorg.libXxf86vm
            xorg.libXi
            xorg.libXtst
          ];

          shellHook = ''
            export JAVA_HOME=${pkgs.jdk21}
            export LD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath (with pkgs; [
              gtk3
              glib
              pango
              cairo
              atk
              gdk-pixbuf
              fontconfig
              freetype
              libGL
              xorg.libX11
              xorg.libXext
              xorg.libXxf86vm
              xorg.libXi
              xorg.libXtst
            ])}:$LD_LIBRARY_PATH"
            echo "☕ Java devshell ready (with JavaFX support) — JDK 21, Maven, Gradle"
          '';
        };
      });
}
