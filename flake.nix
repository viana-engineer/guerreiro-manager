{
  inputs.nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};

        pg-initialize = pkgs.writeShellScriptBin "pg-initialize" ''
          set -eu

          pg_data="$PWD/infra/postgres/data"
          pg_socket="$PWD/infra/postgres/socket"

          mkdir -p "$pg_data" "$pg_socket"

          if [ ! -f "$pg_data/PG_VERSION" ]; then
            initdb -D "$pg_data"
            echo "Cluster PostgreSQL inicializado em $pg_data."
          fi
        '';

        pg-create-example = pkgs.writeShellScriptBin "pg-create-example" ''
          set -eu

          pg_socket="$PWD/infra/postgres/socket"

          if [ "$(psql -h "$pg_socket" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'exemplo'")" != "1" ]; then
            createdb -h "$pg_socket" exemplo
            echo "Banco de dados 'exemplo' criado."
          fi
        '';

        pg-start = pkgs.writeShellScriptBin "pg-start" ''
          set -eu

          ${pg-initialize}/bin/pg-initialize
          pg_ctl -D infra/postgres/data -o "-k $PWD/infra/postgres/socket" start
          ${pg-create-example}/bin/pg-create-example
        '';

        pg-stop = pkgs.writeShellScriptBin "pg-stop" ''
          pg_ctl -D infra/postgres/data stop
        '';

        pg-status = pkgs.writeShellScriptBin "pg-status" ''
          pg_isready -h "$PWD/infra/postgres/socket" -d exemplo
        '';
      in {
        devShells.default = pkgs.mkShell {
          buildInputs = [
            pkgs.nodejs_22
            pkgs.corepack
            pkgs.jdk21
            pkgs.maven
            pkgs.postgresql

            pg-start
            pg-stop
            pg-status
          ];

          shellHook = ''
            export JAVA_HOME=${pkgs.jdk21}
            export PGDATA=$PWD/infra/postgres/data
            export PGHOST=$PWD/infra/postgres/socket
            export PGDATABASE=exemplo
            export PGUSER=$USER

            export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/exemplo
            export SPRING_DATASOURCE_USERNAME=$USER
            export SPRING_DATASOURCE_PASSWORD=

            ${pg-initialize}/bin/pg-initialize

            echo "Ambiente Guerreiro Manager carregado!"
            echo "Node: $(node --version)"
            echo "Java: $(java --version | head -n 1)"
            echo "Maven: $(mvn --version | head -n 1)"
            echo "PostgreSQL: $(psql --version)"
          '';
        };
      }
    );
}
