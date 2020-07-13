# fb_personal_project

Original App Design Project - README
===

# APP_NAME_HERE

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
This app will be a walking app! You can see where other people have been taking walks and you can share your own walks as well. You'll be able to "favorite" walks as well as search for walks in your area and even get suggested walks beased on the ones you've taken and favorited.

### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Social
- **Mobile:** Add and edit, uses camera, only for mobile
- **Story:** Allows users to share fun and different walks so that other people can discover new places to explore and stay active
- **Market:** Anyone who likes to go on walks and hikes or anyone who wants to begin taking on an easier form of exercise
- **Habit:** People can post however often they want, but the habit should be more towards browsing the app for new walks since people are likely not going to be uploading several walks a day
- **Scope:** A pretty narrow scope -- only caters towards walks and hikes; the only functions are viewing, posting, liking, and commenting

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can view and search for walks in specific city
* User can add their own walks
* User can login and create a new account
* User can view details of walk
* User can take pictures to add to walk
* User can view their own profile
* User can "favorite" walks
* User can comment on and add pictures to walks

**Optional Nice-to-have Stories**

* "Favorited" walks can be viewed offline
* User can edit any walks they have already created
* User can choose to make walks public or private
* User can search for walks by tags (ie. "nature", "urban", etc.)
* User can edit their profile
* User can copy a walk and change it up to be their own
* User gets recommendations for walks based on ones they've taken / favorites

### 2. Screen Archetypes

* Home
   * User can view newly added walks nearby
       * filtered by city/ nearby cities
   * User can add their own walks
   * User can make walks public or private [stretch]
   * User gets recommendations for walks based on the ones they've taken / favorites [stretch]
* Search
   * User can search for walks in a specific city
   * User can search for walks by tags [stretch]
* Add
    * User can take pictures and add to walk
    * User can add their own walks
* Profile
    * User can view their own profile
        * contains location, walks created, name, profile image, etc.
    * User can edit their profile [stretch]

* Favorites
    * Exactly like Home except it only displays "favorited " walks
    * "Favorited" walks can be viewed offline [stretch]

* Walk Details
    * User can view details of walk
    * User can edit any walks they have created [stretch]
    * User can copy a walk and change it to be their own
* Login
    * User can login to their account
* New Account
    * User can create a new account

* Commenting 
    * User can comment on and add pictures to different walks

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home
* Search
* Favorites
* Profile

**Flow Navigation** (Screen to Screen)

* Login
   * New Account
   * Home
* New Account
   * Home
* Home
    * Add
    * Walk Details
* Add
    * Walk Details
* Walk Details
    * Commenting
* Favorites
    * Walk Details
* Search
    * Walk Details
* Profile
    * Walk Details

## Wireframes
![](https://i.imgur.com/lqoiMSD.jpg)

<img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 
* Objects:
    * User
    * Post
    * Tags
    * Comments
### Models
#### User
| Property | Type | Description |
|----------|------|------------|
| objectId | String | unique ID for user field |
| updatedAt | Date | date the post was edited |
| createdAt | Date | date the post was created |
| username | String | user's display name |
| password | String | user's account password |
| profileImage | File | user's display image |
| location | String | place where user is located |


#### Post
| Property | Type | Description |
|----------|------|-------------|
| objectId | String | unique ID for post field |
| updatedAt | Date | date the post was edited |
| createdAt | Date | date the post was created |
| user | Pointer to author | pointer to user who created the post |
| likes | Number | number of likes the post has
| location | String | city where the walk is located
| tags | List of pointers to tags | list of tags that is associated with the post |
| comments | List of pointers to comments | list of comments made on the post |
| title | String | name of the walk |

#### Tag
| Property | Type | Description |
|----------|------|-------------|
| posts | List of pointers to posts | list of posts associated with this tag |

#### Comment 
| Property | Type | Description |
|----------|------|-------------|
| objectId | String | unique ID for the Comment field |
| createdAt | Date | date the comment was created |
| user | Pointer to author | pointer to user who wrote the comment |
| anon | Boolean | true/false whether or not the user posted the comment anonymously |

### Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]
