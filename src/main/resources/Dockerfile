# Use a lightweight Nginx image
FROM nginx:alpine
# Set the working directory in the container
WORKDIR /usr/share/nginx/html
#prevent docker boost
#COPY static/stopboost.txt .
# Copy the static files to the Nginx directory
COPY static/index.html .
COPY static/vueScript.js .
COPY static/vueStyles.css .

# Expose port 80 to the outside world
EXPOSE 80

# Start nginx when the container starts
CMD ["nginx", "-g", "daemon off;"]