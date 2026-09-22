FROM node:26-alpine AS build
WORKDIR /web

COPY package.json package-lock.json ./
RUN npm ci

COPY . ./
RUN npx ng build

FROM nginx:1.29-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /web/dist/web/browser /usr/share/nginx/html
EXPOSE 80
