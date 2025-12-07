# Certificate Template System - Visual Examples & Workflow

## 🎯 System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Certificate Service System                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. TEMPLATE DESIGN        2. PREVIEW & TEST      3. GENERATE    │
│  ┌──────────────┐          ┌──────────────┐      ┌────────────┐  │
│  │ Add Elements │  ─────→  │ Input Data   │ ───→ │  Export    │  │
│  │ - Text       │          │ - firstName  │      │  - PNG     │  │
│  │ - Image      │          │ - lastName   │      │  - JPG     │  │
│  │ - Shape      │          │ - date       │      │  - PDF     │  │
│  │ - Table      │          │              │      │            │  │
│  │ - QR Code    │          │ Live Preview │      │ Download   │  │
│  └──────────────┘          └──────────────┘      └────────────┘  │
│         ↑                        ↑                      ↑          │
│    Advanced Editor          Certificate Preview    Export & PDF   │
│    Component               Dialog Component         Generation     │
│                                                                    │
└─────────────────────────────────────────────────────────────────┘
```

## 📐 Template Structure

### Visual Layout

```
GRADUATION CERTIFICATE TEMPLATE
─────────────────────────────────────────

┌─────────────────────────────────────────┐
│                                          │
│     🎓 DIPLOMA OF ACHIEVEMENT 🎓        │  ← Element: Text (Title)
│                                          │     Position: (150, 50)
│     This is to certify that              │
│     {{firstName}} {{lastName}}           │  ← Element: Text (with placeholders)
│     has successfully completed the       │
│     course {{courseName}}                │  ← Element: Text (with placeholder)
│                                          │
│                   [Image Placeholder]    │  ← Element: Image
│                                          │
│     Awarded on {{completionDate}}        │  ← Element: Text (with placeholder)
│                                          │
│                        Signature         │  ← Element: Image
│                                          │
└─────────────────────────────────────────┘
```

## 🎨 Element Types & Rendering

### Text Element
```
┌─ Element Definition ─┐
│ Type: text           │
│ Content:             │
│ "Awarded to:         │
│  {{recipientName}}"  │
│ Font: 24px, Bold     │
│ Color: #000000       │
│ Position: (100, 200) │
└──────────────────────┘
         ↓
    ┌─ Data ─┐
    │ recipientName:  │
    │ "John Smith"    │
    └─────────────────┘
         ↓
┌─ Rendered Output ─┐
│ "Awarded to:       │
│  John Smith"       │
│                    │
│ [Displayed on      │
│  canvas at (100,   │
│  200) with 24px    │
│  bold font]        │
└────────────────────┘
```

### Image Element
```
┌─ Element Definition ─┐
│ Type: image          │
│ imageUrl:            │
│ "/assets/logo.png"   │
│ Position: (300, 100) │
│ Size: (150px, 150px) │
└──────────────────────┘
         ↓
┌─ Rendering ─────────┐
│ [Gray placeholder    │
│  box with           │
│  "[Image]" label    │
│  at position (300,   │
│  100) with 150×150   │
│  dimensions]         │
│                      │
│ (Actual images to    │
│  be implemented)     │
└──────────────────────┘
```

### Shape Element
```
┌─ Element Definition ─────┐
│ Type: shape              │
│ Position: (200, 300)     │
│ Size: (200px, 100px)     │
│ Background: #e3f2fd      │
│ Border: 2px solid #1976d2│
└──────────────────────────┘
         ↓
┌─ Rendered Output ────────┐
│        ┌──────────────┐   │
│        │              │   │
│        │              │   │
│        └──────────────┘   │
│      (Blue box with       │
│       dark blue border)   │
└──────────────────────────┘
```

## 💾 Data Flow

### Step 1: Template Definition
```
Template Object:
{
  id: "template-001",
  name: "Graduation Certificate",
  width: 1000,
  height: 700,
  style: {
    backgroundColor: "#ffffff",
    border: { color: "#000000", thickness: 2, style: "solid" }
  },
  elements: [
    {
      id: "elem-001",
      type: "text",
      content: "GRADUATION CERTIFICATE",
      position: { x: 100, y: 50 },
      size: { width: 800, height: 100 },
      styling: { fontSize: 48, fontWeight: "bold", color: "#000000" }
    },
    {
      id: "elem-002",
      type: "text",
      content: "Awarded to {{firstName}} {{lastName}}",
      position: { x: 100, y: 200 },
      size: { width: 800, height: 50 },
      styling: { fontSize: 28, color: "#000000" }
    },
    {
      id: "elem-003",
      type: "text",
      content: "Course: {{courseName}} | Date: {{completionDate}}",
      position: { x: 100, y: 400 },
      size: { width: 800, height: 50 },
      styling: { fontSize: 16, color: "#666666" }
    }
  ]
}
```

### Step 2: Placeholder Extraction
```
Template → Service.extractPlaceholders()
  ↓
Scan all text elements for {{key}} patterns
  ↓
Found placeholders:
  - firstName
  - lastName
  - courseName
  - completionDate
  ↓
Return: ["firstName", "lastName", "courseName", "completionDate"]
```

### Step 3: Form Generation
```
Placeholders → Create Form Fields

┌──────────────────────────────────────┐
│ Certificate Data Entry Form          │
├──────────────────────────────────────┤
│                                      │
│ firstName: [_________________] {{firstName}}   │
│ lastName:  [_________________] {{lastName}}    │
│ courseName: [_________________] {{courseName}} │
│ completionDate: [__________] {{completionDate}}
│                                      │
│            [Preview] [Generate]      │
│                                      │
└──────────────────────────────────────┘
```

### Step 4: Data Substitution
```
User Input Data:
{
  firstName: "Jane",
  lastName: "Smith",
  courseName: "Advanced Diploma",
  completionDate: "December 5, 2024"
}
  ↓
Apply substitutions to elements:
  - "Awarded to {{firstName}} {{lastName}}"
    becomes
    "Awarded to Jane Smith"
  
  - "Course: {{courseName}} | Date: {{completionDate}}"
    becomes
    "Course: Advanced Diploma | Date: December 5, 2024"
  ↓
Result: Substituted elements array
```

### Step 5: Canvas Rendering
```
Substituted Elements → renderTemplateToCanvas()
  ↓
Canvas Rendering Steps:
1. Set canvas size: 1000×700
2. Fill background: white
3. Draw border: 2px black
4. For each element (sorted by z-index):
   - ELEM-001 (Title):
     Text: "GRADUATION CERTIFICATE"
     Font: 48px bold
     Position: (100, 50)
     Render at (100, 50)
   
   - ELEM-002 (Recipients):
     Text: "Awarded to Jane Smith"
     Font: 28px normal
     Position: (100, 200)
     Render at (100, 200)
   
   - ELEM-003 (Details):
     Text: "Course: Advanced Diploma | Date: December 5, 2024"
     Font: 16px normal
     Position: (100, 400)
     Render at (100, 400)
  ↓
Result: Rendered canvas image
```

### Step 6: Export Options
```
┌─ Rendered Canvas ─┐
│                   │
│ [Certificate]     │
│                   │
└───────────────────┘
   ↙        ↓        ↘
 PNG      JPG      PDF
  ↓        ↓        ↓
[Download] [Download] [Server]
 Local    Local     Generate
 Export   Export    via HTTP
```

## 🎭 User Interface Flow

### Preview Dialog Layout

```
┌────────────────────────────────────────────────────────────────────┐
│ Certificate Preview & Simulation                             [X]   │
├────────────────────────────────────────────────────────────────────┤
│ [Preview] [Data] [Template]                                        │
├────────────────────────────────────────────────────────────────────┤
│                                                                      │
│ ┌─────────────────────┐  ┌──────────────────────────────────────┐  │
│ │ Certificate Data    │  │ Generated Certificate                │  │
│ ├─────────────────────┤  ├──────────────────────────────────────┤  │
│ │                     │  │                                       │  │
│ │ firstName:          │  │   GRADUATION CERTIFICATE              │  │
│ │ [Jane ________]     │  │                                       │  │
│ │ {{firstName}}       │  │   Awarded to Jane Smith               │  │
│ │                     │  │                                       │  │
│ │ lastName:           │  │   Course: Advanced Diploma            │  │
│ │ [Smith ______]      │  │   Date: December 5, 2024              │  │
│ │ {{lastName}}        │  │                                       │  │
│ │                     │  │                                       │  │
│ │ courseName:         │  │                                       │  │
│ │ [Advanced Diploma..]│  │                                       │  │
│ │ {{courseName}}      │  │   [Canvas Rendering]                  │  │
│ │                     │  │                                       │  │
│ │ completionDate:     │  │                                       │  │
│ │ [December 5, 2024]  │  │                                       │  │
│ │ {{completionDate}}  │  │                                       │  │
│ │                     │  └──────────────────────────────────────┘  │
│ │                     │                                             │
│ └─────────────────────┘                                             │
│                                                                      │
│ [PNG Export] [JPG Export] [Generate PDF]                           │
│                                                                      │
└────────────────────────────────────────────────────────────────────┘
```

### Data Tab

```
┌────────────────────────────────────────────────────────────┐
│ [Preview] [Data] [Template]                               │
├────────────────────────────────────────────────────────────┤
│                                                             │
│ JSON Output:                                               │
│ {                                                           │
│   "template": "Graduation Certificate",                    │
│   "data": {                                                │
│     "firstName": "Jane",                                   │
│     "lastName": "Smith",                                   │
│     "courseName": "Advanced Diploma",                      │
│     "completionDate": "December 5, 2024"                   │
│   },                                                       │
│   "elements": [                                            │
│     {                                                      │
│       "id": "elem-001",                                    │
│       "type": "text",                                      │
│       "content": "GRADUATION CERTIFICATE",                 │
│       "position": { "x": 100, "y": 50 },                   │
│       ...                                                  │
│     },                                                     │
│     {                                                      │
│       "id": "elem-002",                                    │
│       "type": "text",                                      │
│       "content": "Awarded to Jane Smith",                  │
│       ...                                                  │
│     }                                                      │
│   ]                                                        │
│ }                                                          │
│                                                             │
└────────────────────────────────────────────────────────────┘
```

### Template Info Tab

```
┌──────────────────────────────────────────────────────────┐
│ [Preview] [Data] [Template]                              │
├──────────────────────────────────────────────────────────┤
│                                                           │
│ Graduation Certificate                                   │
│ Professional diploma template for educational institutions│
│                                                           │
│ Dimensions: 1000px × 700px                               │
│                                                           │
│ Background Color:                                        │
│ ■ #ffffff (white)                                        │
│                                                           │
│ Border:                                                  │
│ 2px solid #000000 (black)                                │
│                                                           │
│ Elements: 3 element(s)                                   │
│ • TEXT - GRADUATION CERTIFICATE                          │
│ • TEXT - Awarded to {{firstName}} {{lastName}}...        │
│ • TEXT - Course: {{courseName}} | Date: {{c...           │
│                                                           │
│ Placeholders:                                            │
│ {{firstName}} {{lastName}} {{courseName}} {{completionDate}}
│                                                           │
└──────────────────────────────────────────────────────────┘
```

## 📊 Placeholder Syntax Examples

### Simple Placeholder
```
Template: "Hello {{name}}"
Data: { name: "World" }
Result: "Hello World"
```

### Multiple Placeholders
```
Template: "{{greeting}} {{name}}, welcome to {{location}}"
Data: { greeting: "Hello", name: "John", location: "Certificate System" }
Result: "Hello John, welcome to Certificate System"
```

### Multi-line Text
```
Template: """
This certifies that {{studentName}}
has completed the {{courseName}} course
on {{completionDate}}
"""
Data: { 
  studentName: "Jane Smith",
  courseName: "Advanced Web Development",
  completionDate: "December 5, 2024"
}
Result: """
This certifies that Jane Smith
has completed the Advanced Web Development course
on December 5, 2024
"""
```

## 🔄 Complete Example Workflow

### 1. Create Template
```
User Action → Advanced Editor
- Click "Add Text"
- Type: "Certificate for {{firstName}} {{lastName}}"
- Drag to position (100, 150)
- Set font size: 28px
- Click "Add Text" again
- Type: "Completed: {{courseName}}"
- Drag to position (100, 250)
- Click "Save"
```

### 2. Preview Certificate
```
User Action → Click "Preview"
Preview Dialog Opens:
- Placeholder extraction finds: [firstName, lastName, courseName]
- Form fields created automatically
- Canvas shows empty template (no data yet)

User Action → Fill Form
- firstName: "John"
- lastName: "Doe"
- courseName: "Angular Development"

Result:
- Canvas updates in real-time
- Shows: "Certificate for John Doe" + "Completed: Angular Development"
```

### 3. Export
```
User Action → Click "Export as PNG"
- Canvas image downloaded as certificate-preview.png

User Action → Click "Generate PDF"
- Server generates professional PDF
- PDF downloaded as certificate.pdf
```

## 🎯 Key Design Patterns

### Pattern 1: Placeholder Detection
```typescript
// Regex pattern for placeholder
/\{\{(\w+)\}\}/g

// Matches:
"Hello {{firstName}}" → firstName
"Date: {{date}}, Name: {{name}}" → date, name

// Non-matches:
"{ single brace }" → no match
"{{{triple}}} braces" → no match
"{{ spaces }}" → matches " spaces "
```

### Pattern 2: Element Rendering Pipeline
```
Element Definition
     ↓
Extract Type
     ↓
Apply Transformation (rotation, opacity)
     ↓
Type-Specific Render
     ├─ Text: Use font context
     ├─ Image: Draw placeholder
     ├─ Shape: Fill rectangle
     ├─ Table: Grid outline
     └─ QR: Placeholder
     ↓
Final Canvas Output
```

## 📱 Responsive Design

### Desktop Layout (>1200px)
```
┌────────────────────────────────────────┐
│ Toolbar                                │
├─────────────────┬──────────────────────┤
│ Form            │ Canvas               │
│ (300px)         │ (900px)              │
│                 │                      │
│ • firstName     │ [Preview Canvas]     │
│ • lastName      │ [200-400px height]   │
│ • courseName    │                      │
│                 │                      │
├─────────────────┴──────────────────────┤
│ Export Buttons                         │
└────────────────────────────────────────┘
```

### Tablet Layout (768-1200px)
```
┌─────────────────────────────┐
│ Toolbar                     │
├─────────────────────────────┤
│ Form                        │
│ • firstName                 │
│ • lastName                  │
│ • courseName                │
│                             │
├─────────────────────────────┤
│ Canvas                      │
│ [Preview Canvas]            │
│                             │
├─────────────────────────────┤
│ [PNG] [JPG] [PDF]          │
└─────────────────────────────┘
```

### Mobile Layout (<768px)
```
┌─────────────────┐
│ Toolbar         │
├─────────────────┤
│ Form            │
│ • firstName     │
│ • lastName      │
│ • courseName    │
│ [Submit]        │
│ [Scroll down]   │
├─────────────────┤
│ Canvas          │
│ [Preview]       │
│ [Scroll down]   │
├─────────────────┤
│ [PNG]           │
│ [JPG]           │
│ [PDF]           │
└─────────────────┘
```

## 🎬 Animation & Transitions

- Form field focus: Highlight on input
- Canvas update: Smooth re-render
- Tab switching: Quick transition
- Export download: Progress feedback

---

This visual guide provides comprehensive understanding of how the certificate template system works, from user interaction to final output.
