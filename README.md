# reservation-app (June 1, 2026)
This document will be used to keep track of the features of the project as well as an extensive documentation for each iteration. Commit messages will be kept reasonably long, and additional information regarding the latest push will be found here. This document will be modified as the project progresses.
## Additions
- Added an initial React/Vite frontend.
	- Customer, Restaurant, and Admin Dashboards
	- Customers can:
		- Make reservations,
		- Add items to their cart,
		- Place orders,
		- View their past and active reservations and their orders for each
	- Restaurants can:
		- Manage (Add/Update/Remove) seating areas,
		- Manage (Confirm/Cancel/Complete) reservations,
		- View past and active reservations made to their restaurant and their orders for each
	- Admins can:
		- Delete restaurant profiles,
		- Delete reservations,
		- Delete reviews
- Customer/Restaurant registration form with password confirmation and ability to hide/show password fields

## Fixes
- Admins can now only be signed up in the backend by administrators
	- Removed admin registration endpoints
- All users can now sign in with their phone numbers as well
- When a user is deleted, their child entities are also deleted (when a restaurant is deleted, all reservations made to that restaurant, and all orders placed to that restaurant will also be deleted)
	- Any other contextually related entities are also deleted (when a restaurant is deleted, its address, menu and its menu's items are also deleted)

## What is Next
- Customers will soon be able to:
	- [ ] Leave reviews using the frontend as well,
	- [ ] View their past reviews (and possibly delete them should they wish),
	- [ ] Change their reservations in using the frontend as well,
	- [ ] Receive notifications upon any reservation changes,
	- [ ] Search for restaurants using filters such as name, city, minimum number of stars or reviews, etc.,
	- [ ] View their **Account Dashboard** to change their account information
- Restaurants will soon be able to:
	- [ ] View their **Account Dashboard** to change their account information,
	- [ ] Change their business hours,
	- [ ] Toggle if they are currently open or closed,
	- [ ] Update menu items (pricing, availability, description, etc.)
	- [ ] Add a photo for their business that can be viewed by customers,
	- [ ] Change their business phone,
	- [ ] Receive notifications upon receiving a reservation (and thus an order with it)
- Admins will soon be able to:
	- [ ] Search for customers, restaurants, reservations, and reviews
- **Account Dashboard:** This will be the main source for users to change their email, phone number, and passwords. This dashboard will also include their notification preferences once it is implemented.