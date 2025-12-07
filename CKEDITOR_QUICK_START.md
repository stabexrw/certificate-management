# Quick Start: CKEditor Certificate System

## Running the Application

### Backend
```bash
cd backend
mvn spring-boot:run
```
Backend will start on `http://localhost:8080`

### Frontend
```bash
cd frontend
npm install  # First time only
ng serve
```
Frontend will start on `http://localhost:4200`

## Creating Your First Certificate Template

### Step 1: Login
- Go to `http://localhost:4200`
- Login with your credentials
- Navigate to **Templates** section

### Step 2: Create New Template
1. Click **"Create New Template"**
2. Fill in:
   - **Name**: e.g., "Course Completion Certificate"
   - **Description**: e.g., "Standard certificate for course completion"

### Step 3: Design Your Certificate

Use the CKEditor toolbar to design:

#### Add a Title
1. Type your title: "Certificate of Completion"
2. Select the text
3. Use toolbar: Heading → Heading 1
4. Center align
5. Change font size to 48
6. Apply color (e.g., dark blue)

#### Add a Border
1. Insert Table (1 row, 1 column)
2. Type content inside the table
3. Right-click table → Table properties
4. Set border width (e.g., 5px)
5. Set border color
6. Add padding (e.g., 20px)

#### Insert Placeholders
Click the suggested placeholder chips or type manually:
- `This certifies that {{student_name}}`
- `has successfully completed {{course_name}}`
- `on {{completion_date}}`
- `Instructor: {{instructor_name}}`

#### Add Images
1. Click image icon in toolbar
2. Upload logo or signature image
3. Resize and position as needed

#### Style Text
- **Bold**: Select text → Bold button
- **Colors**: Use font color picker
- **Backgrounds**: Use background color picker
- **Fonts**: Select from font family dropdown

### Step 4: Save Template
Click **"Save Template"** button

## Testing Your Template

### Step 5: Simulate Certificate
1. Click **"Preview & Simulate"** button
2. You'll see a form with all your placeholders
3. Default values are pre-filled for testing
4. Modify any values as needed
5. Click **"Generate Preview"**

### Step 6: View Preview
- HTML preview shows exactly how the certificate looks
- All styles, colors, borders are preserved
- Placeholders are replaced with test data

### Step 7: Generate PDF
1. Click **"Download PDF"**
2. PDF file downloads automatically
3. Open PDF to verify formatting

## Example Template Design

```html
<!-- Sample certificate layout -->
<div style="border: 5px solid #2c3e50; padding: 40px; text-align: center;">
  <h1 style="color: #2c3e50; font-size: 48px; margin-bottom: 20px;">
    CERTIFICATE OF ACHIEVEMENT
  </h1>
  
  <p style="font-size: 18px; color: #7f8c8d; margin: 20px 0;">
    This is to certify that
  </p>
  
  <h2 style="color: #3498db; font-size: 36px; margin: 20px 0;">
    {{student_name}}
  </h2>
  
  <p style="font-size: 18px; color: #7f8c8d; margin: 20px 0;">
    has successfully completed
  </p>
  
  <h3 style="color: #2c3e50; font-size: 28px; margin: 20px 0;">
    {{course_name}}
  </h3>
  
  <p style="font-size: 16px; color: #95a5a6; margin: 30px 0;">
    Date: {{completion_date}} | Grade: {{grade}}
  </p>
  
  <hr style="border: 1px solid #ecf0f1; margin: 30px 0;">
  
  <p style="font-size: 14px; color: #7f8c8d;">
    Instructor: {{instructor_name}}<br>
    Certificate ID: {{certificate_id}}
  </p>
</div>
```

## Available Placeholders

Common placeholders you can use:
- `{{student_name}}` - Student's full name
- `{{course_name}}` - Course title
- `{{completion_date}}` - Date of completion
- `{{date}}` - General date field
- `{{instructor_name}}` - Instructor's name
- `{{grade}}` - Final grade or score
- `{{certificate_id}}` - Unique certificate ID
- `{{organization_name}}` - Your organization name

You can create custom placeholders by typing `{{your_custom_field}}`

## Tips for Best Results

### Design Tips
1. **Use Tables for Borders**: Create a 1x1 table and style borders for certificate frames
2. **Center Everything**: Use center alignment for formal certificates
3. **Font Hierarchy**: Large heading for title, medium for names, small for details
4. **Color Scheme**: Stick to 2-3 colors for professional look
5. **White Space**: Add padding and margins for clean layout

### Placeholder Tips
1. **Descriptive Names**: Use clear placeholder names like `student_name` not just `name`
2. **Consistent Format**: Stick to lowercase with underscores (snake_case)
3. **Test Values**: Use realistic test data when simulating
4. **Required Fields**: Mark important placeholders clearly in your design

### PDF Tips
1. **A4 Landscape**: Default PDF size is A4 landscape
2. **Safe Zone**: Keep important content within margins
3. **Image Quality**: Use high-resolution images (at least 300 DPI)
4. **Font Sizes**: Minimum 12px for readability in PDF

## Common Issues

### Issue: Placeholder Not Detected
**Solution**: Ensure format is exactly `{{name}}` with no spaces

### Issue: Styles Not Showing in PDF
**Solution**: Use inline styles or CKEditor's style tools (avoid external CSS)

### Issue: Image Not Appearing
**Solution**: Ensure image is uploaded/embedded, not linked externally

### Issue: Border Not Showing
**Solution**: Use table borders or inline border styles

## Next Steps

1. **Edit Templates**: Click edit icon next to any template
2. **Delete Templates**: Remove unused templates
3. **Generate Certificates**: Use templates to create real certificates
4. **Batch Generation**: Generate multiple certificates at once via API

## API Integration

For developers integrating with the API:

```javascript
// Simulate template
POST /api/templates/simulate
{
  "templateId": 1,
  "placeholderValues": {
    "student_name": "John Doe",
    "course_name": "Web Development",
    "completion_date": "2025-12-05"
  }
}

// Generate PDF
POST /api/templates/simulate/pdf
// Same request body, returns PDF blob
```

## Support

Need help?
- Check `CKEDITOR_IMPLEMENTATION_GUIDE.md` for detailed documentation
- Review backend logs for errors
- Use browser DevTools for frontend debugging

---

**Congratulations!** You now have a fully functional certificate template system using CKEditor. Design beautiful certificates with rich formatting, test them with simulation, and generate PDFs with ease.
