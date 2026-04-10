# UI Screen Generation Prompts for IT Infrastructure Asset Management

This document contains detailed prompts for generating UI mockups/wireframes for the IT Infrastructure Asset Management application using AI image generation tools (e.g., Google ImageFX, Midjourney, DALL-E).

## General Design Guidelines

**Design System**: Modern, clean, professional enterprise application
**Color Scheme**: Primary blue (#1976D2), secondary gray (#424242), success green (#4CAF50), warning yellow (#FFC107), danger red (#F44336)
**Typography**: Sans-serif, clear hierarchy
**Layout**: Responsive, card-based design with sidebar navigation
**Components**: Material Design or similar modern UI framework

---

## 1. Login Screen

### Prompt:
```
Create a modern, professional login screen for an IT Infrastructure Asset Management web application. The screen should feature:
- Centered login card with subtle shadow
- Application logo and title "IT Asset Management" at the top
- Two input fields: Username (with user icon) and Password (with lock icon)
- "Remember Me" checkbox
- Primary blue "Login" button
- "Forgot Password?" link below the button
- Clean white background with subtle gradient
- Minimalist design following Material Design principles
- Desktop view, 1920x1080 resolution
- Professional enterprise software aesthetic
```

---

## 2. Dashboard Screen

### Prompt:
```
Create a modern dashboard screen for an IT Infrastructure Asset Management application. The layout should include:
- Left sidebar navigation with icons: Dashboard, My Requests, Assets, Users, Reports, Profile
- Top header bar with application title, notification bell icon with red badge (3), and user avatar
- Main content area with 4 summary cards in a row:
  * Pending Requests (yellow icon, number 5)
  * Approved Requests (blue icon, number 12)
  * Assigned Assets (green icon, number 8)
  * Recent Activity (purple icon, number 15)
- Below cards: "Recent Notifications" panel showing 3 notification items with status badges
- "Quick Actions" section with 3 large buttons: "Create Allocation Request", "Create De-allocation Request", "View All Requests"
- "Recent Tickets" table showing 5 rows with columns: Ticket Number, Type, Asset, Status (colored badges), Date
- Modern, clean design with Material Design components
- Responsive layout, desktop view 1920x1080
- Professional blue and white color scheme
```

---

## 3. My Requests / Ticket List Screen

### Prompt:
```
Create a ticket list screen for an IT Asset Management application showing user's asset requests. The screen should feature:
- Page title "My Requests" with breadcrumb navigation
- Filter bar at top with:
  * Status dropdown (All, Pending, Approved, Rejected, In Progress, Completed, Cancelled)
  * Type dropdown (All, Allocation, De-allocation)
  * Date range picker
  * Search box for ticket number/asset name
  * "Apply Filters" button
- Data table with columns:
  * Ticket Number (clickable link, e.g., TKT-2024-00123)
  * Type (badge: Allocation/De-allocation)
  * Asset Name
  * Status (colored badge: Pending-yellow, Approved-blue, Rejected-red, In Progress-purple, Completed-green)
  * Priority (Low/Medium/High/Urgent with icons)
  * Created Date
  * Last Updated
  * Actions (eye icon for view details)
- 10 rows of sample data
- Pagination controls at bottom (showing "1-10 of 45 items")
- Modern table design with hover effects
- Desktop view, Material Design style
```

---

## 4. Ticket Detail Screen

### Prompt:
```
Create a detailed ticket view screen for an IT Asset Management application. The layout should include:
- Page header with large ticket number "TKT-2024-00123" and back button
- Status badge (Approved - blue) and Type badge (Allocation) prominently displayed
- Priority indicator (High - orange flag icon)
- Left column (60% width):
  * "Asset Information" card showing: Asset Name, Serial Number, Asset Type, Current Status
  * "Request Details" card showing: Requester Name, Email, Request Reason (text area), Assignment To (User/Location), Priority
  * "Approval Information" card showing: Approver Name, Approval Date, Approval Comments
- Right column (40% width):
  * "Timeline" card with vertical timeline showing:
    - Created (timestamp, user icon)
    - Approved (timestamp, checkmark icon)
    - In Progress (timestamp, clock icon)
    - Completed (timestamp, success icon)
- Bottom action buttons: "Cancel Request" (if pending), "Back to My Requests"
- Clean card-based layout with shadows
- Professional design, desktop view
- Material Design components
```

---

## 5. Create Request Screen (Allocation)

### Prompt:
```
Create a request creation form screen for IT asset allocation. The screen should feature:
- Page title "Create Asset Allocation Request"
- Tab navigation at top: "Allocation Request" (active), "De-allocation Request"
- Form layout with sections:
  * "Asset Selection" section:
    - Search/autocomplete field with magnifying glass icon
    - Selected asset card showing: Asset image placeholder, Name, Serial Number, Type, Availability status (green "Available")
  * "Assignment Details" section:
    - Radio buttons: "Assign to User" (selected), "Assign to Location"
    - User search field with autocomplete
    - Email field (auto-filled)
  * "Request Information" section:
    - Request Reason textarea (required indicator *)
    - Priority dropdown (Low, Medium, High, Urgent)
- Bottom action buttons: "Submit Request" (primary blue), "Cancel" (secondary gray)
- Form validation indicators (red asterisks for required fields)
- Clean, spacious form layout
- Desktop view, Material Design style
- Professional enterprise form design
```

---

## 6. User Management List Screen

### Prompt:
```
Create a user management screen for administrators in an IT Asset Management application. The screen should include:
- Page title "User Management" with "Create User" button (blue, with plus icon)
- Search bar and filter controls:
  * Search by username/email
  * Filter by Role dropdown (All, Administrator, Asset Manager, Viewer)
  * Filter by Status dropdown (All, Active, Disabled)
- Data table with columns:
  * Username
  * Email
  * Roles (colored badges: Administrator-red, Asset Manager-blue, Viewer-green)
  * Status (toggle switch: Active/Disabled)
  * Last Login (date/time)
  * Actions (edit icon, enable/disable toggle)
- 8 rows of sample user data
- Pagination controls at bottom
- Modern table with alternating row colors
- Action buttons with hover tooltips
- Desktop view, Material Design
- Professional admin interface aesthetic
```

---

## 7. User Form Screen (Create/Edit User)

### Prompt:
```
Create a user creation/edit form screen for an IT Asset Management application. The screen should feature:
- Page title "Create New User" (or "Edit User")
- Form layout with sections:
  * "Account Information":
    - Username field (with user icon, required *)
    - Email field (with envelope icon, required *)
    - Password field (with lock icon, required for create, optional for edit)
    - Confirm Password field (with lock icon)
    - Password strength indicator bar below password field
  * "Roles & Permissions":
    - Multi-select checkboxes with role descriptions:
      □ Administrator (Full system access)
      □ Asset Manager (Manage assets and approve requests)
      □ Viewer (Read-only access)
  * "Account Status":
    - Toggle switch: Active / Disabled
    - Help text: "Disabled accounts cannot log in"
- Validation messages in red below fields (e.g., "Email already exists")
- Bottom action buttons: "Save User" (primary blue), "Cancel" (secondary gray)
- Clean form layout with proper spacing
- Desktop view, Material Design
- Professional form design with clear visual hierarchy
```

---

## 8. User Profile Screen

### Prompt:
```
Create a user profile screen for self-service profile management. The screen should include:
- Page title "My Profile"
- Profile header section:
  * Large user avatar circle (placeholder with initials)
  * Username (large text)
  * Role badges below username
  * Account status indicator (Active - green)
- Two-column layout:
  * Left column (60%):
    - "Profile Information" card (read-only):
      * Username (with label)
      * Roles (with badges)
      * Account Status
      * Member Since date
    - "Contact Information" card (editable):
      * Email field with edit icon
      * "Update Email" button
  * Right column (40%):
    - "Change Password" card:
      * Current Password field (with lock icon)
      * New Password field (with lock icon)
      * Confirm New Password field (with lock icon)
      * Password strength indicator
      * "Change Password" button
- Clean card-based layout with shadows
- Desktop view, Material Design
- Professional profile page design
```

---

## 9. Asset List Screen

### Prompt:
```
Create an asset inventory list screen for an IT Asset Management application. The screen should feature:
- Page title "Asset Inventory" with "Add Asset" button (blue, with plus icon)
- Filter and search bar:
  * Search box (search by name, serial number)
  * Asset Type dropdown filter (All, Server, Workstation, Laptop, Monitor, Keyboard, Mouse, etc.)
  * Status dropdown filter (All, Ordered, Received, Deployed, In Use, Maintenance, Retired)
  * Location filter
  * "Export" button (download icon)
- Data table with columns:
  * Asset Type (icon + text)
  * Name
  * Serial Number
  * Status (colored badge)
  * Location
  * Assigned To
  * Acquisition Date
  * Actions (view, edit, delete icons)
- 10 rows of diverse asset data (servers, laptops, monitors, keyboards, mice, etc.)
- Pagination controls showing "1-10 of 1,247 items"
- Modern table design with sortable column headers
- Desktop view, Material Design
- Professional inventory management aesthetic
```

---

## 10. Asset Detail Screen

### Prompt:
```
Create an asset detail view screen for an IT Asset Management application. The layout should include:
- Page header with asset name and back button
- Asset type icon and status badge prominently displayed
- Three-column layout:
  * Left column (40%):
    - "Asset Information" card:
      * Asset Type (with icon)
      * Name
      * Serial Number
      * Acquisition Date
      * Current Status (colored badge)
      * Location
      * Notes section
  * Middle column (30%):
    - "Assignment Information" card:
      * Assigned To (user name)
      * Assigned Email
      * Assignment Date
      * "Reassign" button
    - "Lifecycle History" card:
      * Timeline showing status changes with dates
  * Right column (30%):
    - "Quick Actions" card:
      * "Edit Asset" button
      * "Change Status" button
      * "View Audit Log" button
    - "Assignment History" card:
      * List of previous assignments with dates
- Clean card-based layout with shadows
- Desktop view, Material Design
- Professional asset detail page
```

---

## 11. Reports Screen

### Prompt:
```
Create a reports dashboard screen for an IT Asset Management application. The screen should feature:
- Page title "Reports & Analytics"
- Grid layout with report cards (2x2):
  * "Assets by Type" card:
    - Donut chart showing distribution (Server 25%, Workstation 30%, Laptop 20%, Peripherals 25%)
    - "Generate Report" button
  * "Assets by Location" card:
    - Bar chart showing assets per location
    - "Generate Report" button
  * "Assets by Status" card:
    - Horizontal bar chart (In Use, Maintenance, Storage, etc.)
    - "Generate Report" button
  * "End-of-Life Assets" card:
    - Number indicator (45 assets)
    - List of asset types approaching EOL
    - "Generate Report" button
- "Ticket Metrics" section below:
  * Summary cards: Total Tickets, Approval Rate, Avg. Completion Time
  * Line chart showing ticket trends over time
- Modern dashboard with charts and visualizations
- Desktop view, Material Design
- Professional analytics interface
```

---

## 12. Mobile View - Dashboard

### Prompt:
```
Create a mobile responsive dashboard view for an IT Asset Management application. The screen should feature:
- Mobile viewport (375x812 - iPhone size)
- Top header with hamburger menu icon, app title, notification bell with badge
- Summary cards stacked vertically (full width):
  * Pending Requests card (yellow accent)
  * Approved Requests card (blue accent)
  * Assigned Assets card (green accent)
- "Quick Actions" section with two large buttons stacked:
  * "Create Request" button
  * "View My Requests" button
- "Recent Tickets" section showing 3 ticket cards (not table):
  * Each card shows: Ticket number, Status badge, Asset name, Date
  * Tap to view details
- Bottom navigation bar with 5 icons: Dashboard, Requests, Assets, Profile, More
- Touch-friendly spacing and button sizes
- Material Design mobile components
- Clean, modern mobile interface
```

---

## 13. Mobile View - Ticket List

### Prompt:
```
Create a mobile responsive ticket list view for an IT Asset Management application. The screen should feature:
- Mobile viewport (375x812 - iPhone size)
- Top header with back arrow, "My Requests" title, filter icon
- Filter chips below header (horizontally scrollable):
  * "All Status" chip
  * "Allocation" chip
  * "Pending" chip (selected - blue)
  * "This Month" chip
- Ticket cards stacked vertically (full width):
  * Each card contains:
    - Ticket number (top left)
    - Status badge (top right, colored)
    - Asset name (bold)
    - Type badge (Allocation/De-allocation)
    - Priority indicator
    - Date (bottom)
    - Right arrow icon
- 5 ticket cards visible
- Floating action button (FAB) at bottom right with plus icon
- Pull-to-refresh indicator at top
- Touch-friendly card spacing
- Material Design mobile components
- Modern mobile list interface
```

---

## 14. Tablet View - Asset Management

### Prompt:
```
Create a tablet responsive view for asset management in an IT Asset Management application. The screen should feature:
- Tablet viewport (768x1024 - iPad size)
- Left sidebar navigation (collapsed, icons only)
- Top header with app title, search bar, notification bell, user avatar
- Main content area with "Asset Inventory" title
- Filter bar with dropdowns (Asset Type, Status, Location) and search
- Data table optimized for tablet:
  * Columns: Type (icon), Name, Serial Number, Status (badge), Assigned To, Actions
  * 8 rows visible
  * Horizontal scroll for additional columns
- "Add Asset" floating action button (bottom right)
- Pagination controls at bottom
- Touch-friendly table rows with adequate spacing
- Material Design tablet components
- Professional tablet interface
- Landscape orientation
```

---

## 15. Notification Panel

### Prompt:
```
Create a notification dropdown panel for an IT Asset Management application. The panel should feature:
- Dropdown panel (350px wide) appearing from notification bell icon
- Header with "Notifications" title and "Mark all as read" link
- List of 5 notification items:
  * Each notification shows:
    - Icon (checkmark for approved, X for rejected, clock for pending)
    - Notification text (e.g., "Your request TKT-2024-00123 has been approved")
    - Time ago (e.g., "2 hours ago")
    - Unread indicator (blue dot) for unread items
    - Hover effect showing light background
- "View All Notifications" link at bottom
- Clean, modern dropdown design
- Material Design components
- Subtle shadow and border
- Professional notification UI
```

---

## Usage Instructions

1. **For AI Image Generation Tools**: Copy the prompt text for the desired screen
2. **Customize**: Adjust colors, dimensions, or specific details as needed
3. **Iterate**: Generate multiple variations and refine prompts based on results
4. **Consistency**: Use the same design system keywords across all prompts for consistent results

## Design System Reference

- **Primary Color**: #1976D2 (Blue)
- **Secondary Color**: #424242 (Dark Gray)
- **Success**: #4CAF50 (Green)
- **Warning**: #FFC107 (Yellow/Amber)
- **Danger**: #F44336 (Red)
- **Info**: #2196F3 (Light Blue)
- **Background**: #FAFAFA (Light Gray)
- **Card Background**: #FFFFFF (White)
- **Text Primary**: #212121 (Almost Black)
- **Text Secondary**: #757575 (Gray)

## Status Badge Colors

- **Pending**: Yellow (#FFC107)
- **Approved**: Blue (#2196F3)
- **Rejected**: Red (#F44336)
- **In Progress**: Purple (#9C27B0)
- **Completed**: Green (#4CAF50)
- **Cancelled**: Gray (#9E9E9E)
