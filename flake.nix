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

          run_dir="$GUERREIRO_ROOT/.direnv/run"
          pid_file="$run_dir/api.pid"

          mkdir -p "$run_dir"

          if [ -f "$pid_file" ] && kill -0 -- "-$(cat "$pid_file")" 2>/dev/null; then
            echo "A API já está em execução (PID $(cat "$pid_file"))."
            exit 1
          fi

          rm -f "$pid_file"
          cd "$GUERREIRO_ROOT/backend"
          setsid mvn spring-boot:run &
          service_pid=$!
          echo "$service_pid" > "$pid_file"

          cleanup() {
            rm -f "$pid_file"
          }

          stop_service() {
            kill -TERM -- "-$service_pid" 2>/dev/null || true
          }

          trap cleanup EXIT
          trap 'stop_service; exit 130' INT
          trap 'stop_service; exit 143' TERM

          echo "API iniciada (PID $service_pid)."
          wait "$service_pid"
        '';

        api-status = pkgs.writeShellScriptBin "api-status" ''
          pid_file="$GUERREIRO_ROOT/.direnv/run/api.pid"

          if [ -f "$pid_file" ] && kill -0 -- "-$(cat "$pid_file")" 2>/dev/null; then
            echo "A API está em execução (PID $(cat "$pid_file"))."
            exit 0
          fi

          rm -f "$pid_file"
          echo "A API está parada."
          exit 1
        '';

        api-stop = pkgs.writeShellScriptBin "api-stop" ''
          set -eu

          pid_file="$GUERREIRO_ROOT/.direnv/run/api.pid"

          if [ ! -f "$pid_file" ] || ! kill -0 -- "-$(cat "$pid_file")" 2>/dev/null; then
            rm -f "$pid_file"
            echo "A API já está parada."
            exit 0
          fi

          service_pid="$(cat "$pid_file")"
          kill -TERM -- "-$service_pid"
          echo "Comando de parada enviado para a API (PID $service_pid)."
        '';

        web-start = pkgs.writeShellScriptBin "web-start" ''
          set -eu

          run_dir="$GUERREIRO_ROOT/.direnv/run"
          pid_file="$run_dir/web.pid"

          mkdir -p "$run_dir"

          if [ -f "$pid_file" ] && kill -0 -- "-$(cat "$pid_file")" 2>/dev/null; then
            echo "A aplicação web já está em execução (PID $(cat "$pid_file"))."
            exit 1
          fi

          rm -f "$pid_file"
          cd "$GUERREIRO_ROOT/frontend"
          setsid npm run dev &
          service_pid=$!
          echo "$service_pid" > "$pid_file"

          cleanup() {
            rm -f "$pid_file"
          }

          stop_service() {
            kill -TERM -- "-$service_pid" 2>/dev/null || true
          }

          trap cleanup EXIT
          trap 'stop_service; exit 130' INT
          trap 'stop_service; exit 143' TERM

          echo "Aplicação web iniciada (PID $service_pid)."
          wait "$service_pid"
        '';

        web-status = pkgs.writeShellScriptBin "web-status" ''
          pid_file="$GUERREIRO_ROOT/.direnv/run/web.pid"

          if [ -f "$pid_file" ] && kill -0 -- "-$(cat "$pid_file")" 2>/dev/null; then
            echo "A aplicação web está em execução (PID $(cat "$pid_file"))."
            exit 0
          fi

          rm -f "$pid_file"
          echo "A aplicação web está parada."
          exit 1
        '';

        web-stop = pkgs.writeShellScriptBin "web-stop" ''
          set -eu

          pid_file="$GUERREIRO_ROOT/.direnv/run/web.pid"

          if [ ! -f "$pid_file" ] || ! kill -0 -- "-$(cat "$pid_file")" 2>/dev/null; then
            rm -f "$pid_file"
            echo "A aplicação web já está parada."
            exit 0
          fi

          service_pid="$(cat "$pid_file")"
          kill -TERM -- "-$service_pid"
          echo "Comando de parada enviado para a aplicação web (PID $service_pid)."
        '';

        guerreiro-help = pkgs.writeShellScriptBin "guerreiro-help" ''
          echo "Comandos do ambiente Guerreiro Manager:"
          echo "  pg-start      Inicia o PostgreSQL e cria o banco se necessário"
          echo "  pg-status     Verifica o estado do PostgreSQL"
          echo "  pg-stop       Encerra o PostgreSQL"
          echo "  api-start     Inicia a API Spring Boot com Maven"
          echo "  api-status    Verifica o estado da API"
          echo "  api-stop      Encerra a API"
          echo "  web-start     Inicia a aplicação web Next.js"
          echo "  web-status    Verifica o estado da aplicação web"
          echo "  web-stop      Encerra a aplicação web"
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
            pkgs.util-linux

            pg-start
            pg-stop
            pg-status
            api-start
            api-status
            api-stop
            web-start
            web-status
            web-stop
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
