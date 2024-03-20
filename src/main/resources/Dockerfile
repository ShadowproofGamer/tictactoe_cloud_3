# Use a lightweight Nginx image
FROM nginx:alpine

# Set the working directory in the container
WORKDIR /usr/share/nginx/html
# Copy the static files to the Nginx directory
COPY static .

# Expose port 80 to the outside world
EXPOSE 80

# Start nginx when the container starts
CMD ["nginx", "-g", "daemon off;"]