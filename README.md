# MindMyApp

**MindMyApp** is an Android application designed to display GitHub organizations using the GitHub REST API. It showcases a clean and modern architecture based on the Model-View-Intent (MVI) pattern and incorporates various best practices and modern development tools.

## Features

- **List of GitHub Organizations**: Fetch and display organizations from the GitHub API.
- **Favorite Organizations**: Mark organizations as favorites and store them locally using Room.
- **Search Functionality**: Search for specific GitHub organizations.

## Technical Overview

- **Architecture**: MVI (Model-View-Intent) pattern with unidirectional data flow.
- **Dependency Injection**: Koin.
- **View Layer**: Jetpack Compose.
- **Local Storage**: Room database for storing favorite organizations.
- **Networking**: Ktor for making REST API requests.
- **State Management**: Kotlin Coroutines and Flow for asynchronous operations and reactive state handling.
- **Unit Testing**: MockK for mocking dependencies and ensuring reliable unit tests.

## Requirements

- **API Token**: To run the application, you need to add a valid GitHub API token in `local.properties`:
  ```properties
  API_TOKEN="your_github_api_token"
  ```

## MVI Architecture Summary

MindMyApp follows the Model-View-Intent (MVI) architecture, which provides predictable state management and a unidirectional data flow. This architecture makes it easier to manage complex UI states and ensures clear separation of concerns.

### Core Components

1. **Model**
    - Manages the application's state and business logic.
    - Processes user events and creates new immutable states.

2. **View**
    - Displays the UI based on the current state.
    - Reacts to state changes and sends user events to the Model.

3. **Intent / Event**
    - Represents user actions or events (e.g., button clicks).
    - Triggers state changes in the Model.

### Unidirectional Data Flow

- User actions generate events that flow from the View to the Model.
- The Model processes the events and emits new states.
- The View observes state changes and updates the UI accordingly.

### State Management and Immutability

- **Single Source of Truth**: The Model holds a single, consistent state.
- **Immutable State**: Each state is immutable, ensuring a clear history of changes.
- **Reactive Updates**: The View reacts to state changes emitted by the Model.

### Side Effects

Side effects are operations triggered by events that do not directly result in UI state changes. These include tasks such as network requests, database queries, or triggering system processes.

Types of side effects:
- **Unexposed Side Effects**: Handled internally by the Model, e.g., repository calls.
- **Exposed Side Effects**: Require the View to trigger processes like navigation or system interactions.

## Pros & Cons of MVI

### Pros
- **Predictable State Changes**: State changes are triggered by explicit events, making the app's behavior more predictable.
- **Improved Debugging**: Unidirectional data flow and immutable state simplify debugging.
- **Decoupled Components**: Clear separation between Model, View, and Intent improves modularity.
- **Ease of Testing**: The Model's logic can be easily unit-tested.

### Cons
- **Increased Complexity**: MVI can add complexity in simpler applications.
- **Learning Curve**: Requires understanding of reactive programming and state management.
