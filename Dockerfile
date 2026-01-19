# Dockerfile for Muehle Game with GUI support
FROM sbtscala/scala-sbt:eclipse-temurin-21.0.5_11_1.10.7_3.6.2

# Install X11/GUI libraries for ScalaFX
RUN apt-get update && apt-get install -y \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1 \
    libgtk-3-0 \
    openjfx \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy build files first (for better caching)
COPY build.sbt .
COPY project/ project/

# Download dependencies (cached layer)
RUN sbt update

# Copy source code
COPY src/ src/

# Compile the project
RUN sbt compile

# Set display environment variable (will be overridden at runtime)
ENV DISPLAY=:0

# Run the application
CMD ["sbt", "run"]
