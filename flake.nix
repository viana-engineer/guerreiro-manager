{
  inputs.nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        databaseName = "guerreiro_db";

        pg-initialize = pkgs.writeShellScriptBin "pg-initialize" ''
          set -eu

          pg_data="$GUERREIRO_ROOT/infra/postgres/data"
          pg_socket="$GUERREIRO_ROOT/infra/postgres/socket"

          mkdir -p "$pg_data" "$pg_socket"

          if [ ! -f "$pg_data/PG_VERSION" ]; then
            initdb -D "$pg_data"
            echo "Cluster PostgreSQL inicializado em $pg_data."
          fi
        '';

        pg-create-database = pkgs.writeShellScriptBin "pg-create-database" ''
          set -eu

          pg_socket="$GUERREIRO_ROOT/infra/postgres/socket"

          if [ "$(psql -h "$pg_socket" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '${databaseName}'")" != "1" ]; then
            createdb -h "$pg_socket" ${databaseName}
            echo "Banco de dados '${databaseName}' criado."
          fi
        '';

        pg-start = pkgs.writeShellScriptBin "pg-start" ''
          set -eu

          pg_data="$GUERREIRO_ROOT/infra/postgres/data"
          pg_socket="$GUERREIRO_ROOT/infra/postgres/socket"

          ${pg-initialize}/bin/pg-initialize
          pg_ctl -D "$pg_data" -o "-k $pg_socket" start
          ${pg-create-database}/bin/pg-create-database
        '';

        pg-stop = pkgs.writeShellScriptBin "pg-stop" ''
          pg_ctl -D "$GUERREIRO_ROOT/infra/postgres/data" stop
        '';

        pg-status = pkgs.writeShellScriptBin "pg-status" ''
          pg_isready -h "$GUERREIRO_ROOT/infra/postgres/socket" -d ${databaseName}
        '';

        api-start = pkgs.writeShellScriptBin "api-start" ''
          set -eu

          cd "$GUERREIRO_ROOT/backend"
          exec mvn spring-boot:run
        '';

        web-start = pkgs.writeShellScriptBin "web-start" ''
          set -eu

          cd "$GUERREIRO_ROOT/frontend"
          exec npm run dev
        '';

        guerreiro-help = pkgs.writeShellScriptBin "guerreiro-help" ''
          echo "Comandos do ambiente Guerreiro Manager:"
          echo "  pg-start      Inicia o PostgreSQL e cria o banco se necessário"
          echo "  pg-status     Verifica o estado do PostgreSQL"
          echo "  pg-stop       Encerra o PostgreSQL"
          echo "  api-start     Inicia a API Spring Boot com Maven"
          echo "  web-start     Inicia a aplicação web Next.js"
        '';
      in {
        devShells.default = pkgs.mkShell {
          buildInputs = [
            pkgs.nodejs_22
            pkgs.corepack
            pkgs.git
            pkgs.jdk21
            pkgs.maven
            pkgs.postgresql

            pg-start
            pg-stop
            pg-status
            api-start
            web-start
            guerreiro-help
          ];

          shellHook = ''
            export JAVA_HOME=${pkgs.jdk21}
            export GUERREIRO_ROOT="$(git rev-parse --show-toplevel)"

            export PGDATA="$GUERREIRO_ROOT/infra/postgres/data"
            export PGHOST="$GUERREIRO_ROOT/infra/postgres/socket"
            export PGDATABASE=${databaseName}
            export PGUSER=$USER

            export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/${databaseName}
            export SPRING_DATASOURCE_USERNAME=$USER
            export SPRING_DATASOURCE_PASSWORD=

            ${pg-initialize}/bin/pg-initialize

            echo "Ambiente Guerreiro Manager carregado!"
            echo "Node: $(node --version)"
            echo "Java: $(java --version | head -n 1)"
            echo "Maven: $(mvn --version | head -n 1)"
            echo "PostgreSQL: $(psql --version)"
            echo "Use 'guerreiro-help' para listar os comandos disponíveis."
          '';
        };
      }
    );
}
