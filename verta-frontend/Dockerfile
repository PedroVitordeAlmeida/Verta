# ===== Etapa 1: build =====
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm install

COPY . .

# VITE_API_URL precisa existir NA HORA DO BUILD (o Vite grava esse valor
# dentro dos arquivos .js finais, nao le-lo em tempo de execucao). Por isso
# e um ARG de build, e quem for montar o docker-compose passa via
# "build: args:" apontando pro endereco publico/interno do backend.
# ARG VITE_API_URL=http://localhost:8080
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build

# ===== Etapa 2: runtime (Nginx servindo os arquivos estaticos) =====
FROM nginx:1.27-alpine AS runtime

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
