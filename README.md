# RadioPlan

A Java application with GUI for radio planning.

Created as a assignment to a course @ Umeå Univeristy and submitted in 2024.

![Placeholder Image](/images/screenshot.png)

## Prerequisites

- Java Development Kit (JDK) 8 or higher

## Project Structure

```
RadioPlan/
├── src/                    # Source code
│   ├── Main.java          # Main application entry point
│   ├── Gui.java           # GUI components
│   ├── images/            # Image resources
│   └── META-INF/          # Manifest file
├── lib/                   # External dependencies
│   └── flatlaf-intellij-themes-3.2.5.jar
└── out/                   # Build output directory (created during build)
```

## How to Compile and Build

### Step 1: Compile the Java Source Files

This is done by running the build shell script in the root directory:

```bash
bash build.sh
```

### Step 2: Run the Application

```bash
java -jar out/RadioPlan.jar
```