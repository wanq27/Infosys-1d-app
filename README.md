## 50.001 1D Project 

#### The objective is to design a prototype android app, applying concepts learnt in the course for effective programming. E.g., Object-oriented design, inheritance, polymorphism, encapsulation, abstract class, interface, generic programming, separation of concerns, loose coupling, various design patterns.

Our group came up with the app **UPSutd** which serves as a central platform for students in an educational institution to share resources, allowing for upcycling of spare materials.

### System Architecture
As seen in the diagram below, our system adopts the serverless architecture as we rely solely on Firebase for our application database without other nodes for the storage and retrieval of data. On the client-side (application), users can interact directly with Firebase, initiating requests for the data they require (viewing listings) and generating new data as necessary (uploading new listings). These data are then either retrieved or written to Firebase as needed. Below is a detailed overview of our database structure. 

>A point to note is that the data stored using SharedPreferences on the client side is not stored in the database. This data stores the user-entered email address during login for verification purposes later on in Firebase’s passwordless sign-in.

<img src="https://github.com/wanq27/Infosys-1d-app/assets/118462447/eee96bec-33d0-41d6-9693-050ca035d748" width=800/>

**Database Structure**
<br />
In this architecture, the Firebase serves as both our backend and database provider. We have utilised 4 main services from Firebase: 
- Firestore for data storage and retrieval
- Cloud Storage for storing user images / listing images
- Authentication for account management
- Cloud Messaging for notifications

The database is set up with 4 main collections: deals, imageLabels, items and users. Each collection contains documents that are uniquely identified by a document ID. Each document is a set of key-value pairs, where each key is a field name and each value is the data associated with that field. 


### Flow

<img src="https://github.com/wanq27/Infosys-1d-app/assets/118462447/baebf192-38ae-4f90-a9a5-3e8bc2ccd8f3" width=800/>


### Main Features

**1. Onboarding**
<br />
Users can sign in to the application using their SUTD email credentials. The authentication process is handled by Firebase and secure access is ensured through backend encryption.

<img src="https://github.com/wanq27/Infosys-1d-app/assets/118462447/7e9f8a14-174b-4b50-9cea-766d1ba4afdd" width=200/>  <img src="https://github.com/wanq27/Infosys-1d-app/assets/118462447/5bfc85d4-ada4-4dc5-a9c2-e487ca37700c" width=200/>


**2. Marketplace**
<br />
The marketplace screen shows all listings that users have uploaded. Users can scroll through the list as well as apply a filter for easier searching and finding of items that they want. This allows users to browse and find any specific item that is available now.

<img src="https://github.com/wanq27/Infosys-1d-app/assets/118462447/0cf732c0-ee9b-47c6-ab8f-ffbc9eac5fb8" width=200/>


**3. Upload Listing**
<br />
Users can list their items by clicking on the “+” icon at the top right of the marketplace page. They will be prompted to list their item details and put it up for other users to see.

<img src="https://github.com/wanq27/Infosys-1d-app/assets/118462447/ef458518-6e18-49ba-9e0d-a806a195be59" width=200/>


**4. Deals**
<br />
This feature facilitates the negotiation and arrangement of deals between buyers and sellers. Users can send and receive deal requests, negotiate terms, and finalise agreements for the exchange of items. 

<img src="https://github.com/wanq27/Infosys-1d-app/assets/118462447/6813463e-41a6-4630-99ea-cc586c993713" width=200/>


**5. View Listing**
<br />
This page allows the user to see detailed information about the item, as well as to ask questions and request a deal. This page has a communal Q&A section which allows all users to see what has been asked and answered about the item, to further add to the communal aspect of our app. Upon clicking the get it now button on the item, user will be directed to select their dealing preferences which will then be sent to the item lister.

<img src="https://github.com/wanq27/Infosys-1d-app/assets/118462447/a0df587e-bfc9-43f2-8f5e-45c075432c3a" width=800/>

