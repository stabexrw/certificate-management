import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CKEditorModule } from '@ckeditor/ckeditor5-angular';
import { 
  ClassicEditor,
  AccessibilityHelp,
  Alignment,
  Autoformat,
  AutoImage,
  AutoLink,
  Autosave,
  BalloonToolbar,
  Base64UploadAdapter,
  BlockQuote,
  Bold,
  Code,
  CodeBlock,
  Essentials,
  FindAndReplace,
  FontBackgroundColor,
  FontColor,
  FontFamily,
  FontSize,
  FullPage,
  GeneralHtmlSupport,
  Heading,
  Highlight,
  HorizontalLine,
  HtmlComment,
  HtmlEmbed,
  ImageBlock,
  ImageCaption,
  ImageInline,
  ImageInsert,
  ImageInsertViaUrl,
  ImageResize,
  ImageStyle,
  ImageTextAlternative,
  ImageToolbar,
  ImageUpload,
  Indent,
  IndentBlock,
  Italic,
  Link,
  LinkImage,
  List,
  ListProperties,
  MediaEmbed,
  Mention,
  PageBreak,
  Paragraph,
  PasteFromOffice,
  RemoveFormat,
  SelectAll,
  ShowBlocks,
  SourceEditing,
  SpecialCharacters,
  SpecialCharactersArrows,
  SpecialCharactersCurrency,
  SpecialCharactersEssentials,
  SpecialCharactersLatin,
  SpecialCharactersMathematical,
  SpecialCharactersText,
  Strikethrough,
  Style,
  Subscript,
  Superscript,
  Table,
  TableCaption,
  TableCellProperties,
  TableColumnResize,
  TableProperties,
  TableToolbar,
  TextTransformation,
  Title,
  TodoList,
  Underline,
  Undo,
  WordCount
} from 'ckeditor5';

import 'ckeditor5/ckeditor5.css';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TemplateService } from '../../../core/services/template.service';

@Component({
  selector: 'app-ckeditor-template-editor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    CKEditorModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatChipsModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './ckeditor-template-editor.component.html',
  styleUrls: ['./ckeditor-template-editor.component.scss']
})
export class CkeditorTemplateEditorComponent implements OnInit {
  public Editor = ClassicEditor;
  public editorConfig: any = {
    licenseKey: 'GPL',
    plugins: [
      AccessibilityHelp,
      Alignment,
      Autoformat,
      AutoImage,
      AutoLink,
      Autosave,
      BalloonToolbar,
      Base64UploadAdapter,
      BlockQuote,
      Bold,
      Code,
      CodeBlock,
      Essentials,
      FindAndReplace,
      FontBackgroundColor,
      FontColor,
      FontFamily,
      FontSize,
      FullPage,
      GeneralHtmlSupport,
      Heading,
      Highlight,
      HorizontalLine,
      HtmlComment,
      HtmlEmbed,
      ImageBlock,
      ImageCaption,
      ImageInline,
      ImageInsert,
      ImageInsertViaUrl,
      ImageResize,
      ImageStyle,
      ImageTextAlternative,
      ImageToolbar,
      ImageUpload,
      Indent,
      IndentBlock,
      Italic,
      Link,
      LinkImage,
      List,
      ListProperties,
      MediaEmbed,
      Mention,
      PageBreak,
      Paragraph,
      PasteFromOffice,
      RemoveFormat,
      SelectAll,
      ShowBlocks,
      SourceEditing,
      SpecialCharacters,
      SpecialCharactersArrows,
      SpecialCharactersCurrency,
      SpecialCharactersEssentials,
      SpecialCharactersLatin,
      SpecialCharactersMathematical,
      SpecialCharactersText,
      Strikethrough,
      Style,
      Subscript,
      Superscript,
      Table,
      TableCaption,
      TableCellProperties,
      TableColumnResize,
      TableProperties,
      TableToolbar,
      TextTransformation,
      Title,
      TodoList,
      Underline,
      Undo,
      WordCount
    ],
    toolbar: {
      items: [
        'sourceEditing',
        'showBlocks',
        '|',
        'undo',
        'redo',
        '|',
        'findAndReplace',
        'selectAll',
        '|',
        'heading',
        'style',
        '|',
        'fontSize',
        'fontFamily',
        'fontColor',
        'fontBackgroundColor',
        '|',
        'bold',
        'italic',
        'underline',
        'strikethrough',
        'code',
        'subscript',
        'superscript',
        'removeFormat',
        '|',
        'specialCharacters',
        'horizontalLine',
        'pageBreak',
        'link',
        'insertImage',
        'mediaEmbed',
        'insertTable',
        'highlight',
        'blockQuote',
        'codeBlock',
        'htmlEmbed',
        '|',
        'alignment',
        '|',
        'bulletedList',
        'numberedList',
        'todoList',
        'outdent',
        'indent',
        '|',
        'accessibilityHelp'
      ],
      shouldNotGroupWhenFull: true
    },
    balloonToolbar: ['bold', 'italic', '|', 'link', 'insertImage', '|', 'bulletedList', 'numberedList'],
    fontSize: {
      options: [9, 10, 11, 12, 13, 14, 15, 16, 18, 20, 22, 24, 26, 28, 30, 32, 36, 40, 44, 48, 54, 60, 66, 72, 80, 88, 96],
      supportAllValues: true
    },
    fontFamily: {
      options: [
        'default',
        'Arial, Helvetica, sans-serif',
        'Courier New, Courier, monospace',
        'Georgia, serif',
        'Lucida Sans Unicode, Lucida Grande, sans-serif',
        'Tahoma, Geneva, sans-serif',
        'Times New Roman, Times, serif',
        'Trebuchet MS, Helvetica, sans-serif',
        'Verdana, Geneva, sans-serif',
        'Comic Sans MS, cursive',
        'Impact, Charcoal, sans-serif',
        'Palatino Linotype, Book Antiqua, Palatino, serif',
        'Garamond, serif',
        'Bookman, serif',
        'Arial Black, sans-serif'
      ],
      supportAllValues: true
    },
    fontColor: {
      columns: 12,
      colors: [
        { color: '#000000', label: 'Black' },
        { color: '#434343', label: 'Dark Grey 4' },
        { color: '#666666', label: 'Dark Grey 3' },
        { color: '#999999', label: 'Dark Grey 2' },
        { color: '#B7B7B7', label: 'Dark Grey 1' },
        { color: '#CCCCCC', label: 'Grey' },
        { color: '#D9D9D9', label: 'Light Grey 1' },
        { color: '#EFEFEF', label: 'Light Grey 2' },
        { color: '#F3F3F3', label: 'Light Grey 3' },
        { color: '#FFFFFF', label: 'White', hasBorder: true },
        { color: '#980000', label: 'Red Berry' },
        { color: '#FF0000', label: 'Red' },
        { color: '#FF9900', label: 'Orange' },
        { color: '#FFFF00', label: 'Yellow', hasBorder: true },
        { color: '#00FF00', label: 'Green' },
        { color: '#00FFFF', label: 'Cyan' },
        { color: '#4A86E8', label: 'Cornflower Blue' },
        { color: '#0000FF', label: 'Blue' },
        { color: '#9900FF', label: 'Purple' },
        { color: '#FF00FF', label: 'Magenta' }
      ]
    },
    fontBackgroundColor: {
      columns: 12,
      colors: [
        { color: '#000000', label: 'Black' },
        { color: '#434343', label: 'Dark Grey 4' },
        { color: '#666666', label: 'Dark Grey 3' },
        { color: '#999999', label: 'Dark Grey 2' },
        { color: '#B7B7B7', label: 'Dark Grey 1' },
        { color: '#CCCCCC', label: 'Grey' },
        { color: '#D9D9D9', label: 'Light Grey 1' },
        { color: '#EFEFEF', label: 'Light Grey 2' },
        { color: '#F3F3F3', label: 'Light Grey 3' },
        { color: '#FFFFFF', label: 'White', hasBorder: true },
        { color: '#980000', label: 'Red Berry' },
        { color: '#FF0000', label: 'Red' },
        { color: '#FF9900', label: 'Orange' },
        { color: '#FFFF00', label: 'Yellow', hasBorder: true },
        { color: '#00FF00', label: 'Green' },
        { color: '#00FFFF', label: 'Cyan' },
        { color: '#4A86E8', label: 'Cornflower Blue' },
        { color: '#0000FF', label: 'Blue' },
        { color: '#9900FF', label: 'Purple' },
        { color: '#FF00FF', label: 'Magenta' }
      ]
    },
    heading: {
      options: [
        { model: 'heading1', view: 'h1', title: 'Heading 1', class: 'ck-heading_heading1' },
        { model: 'heading2', view: 'h2', title: 'Heading 2', class: 'ck-heading_heading2' },
        { model: 'heading3', view: 'h3', title: 'Heading 3', class: 'ck-heading_heading3' },
        { model: 'heading4', view: 'h4', title: 'Heading 4', class: 'ck-heading_heading4' },
        { model: 'heading5', view: 'h5', title: 'Heading 5', class: 'ck-heading_heading5' },
        { model: 'heading6', view: 'h6', title: 'Heading 6', class: 'ck-heading_heading6' }
      ]
    },
    htmlSupport: {
      allow: [
        {
          name: /^.*$/,
          styles: true,
          attributes: true,
          classes: true
        }
      ]
    },
    image: {
      toolbar: [
        'toggleImageCaption',
        'imageTextAlternative',
        '|',
        'imageStyle:inline',
        'imageStyle:wrapText',
        'imageStyle:breakText',
        '|',
        'resizeImage'
      ]
    },
    link: {
      addTargetToExternalLinks: true,
      defaultProtocol: 'https://',
      decorators: {
        toggleDownloadable: {
          mode: 'manual',
          label: 'Downloadable',
          attributes: {
            download: 'file'
          }
        },
        openInNewTab: {
          mode: 'manual',
          label: 'Open in new tab',
          defaultValue: true,
          attributes: {
            target: '_blank',
            rel: 'noopener noreferrer'
          }
        }
      }
    },
    list: {
      properties: {
        styles: true,
        startIndex: true,
        reversed: true
      }
    },
    mention: {
      feeds: [
        {
          marker: '{',
          feed: ['student_name', 'course_name', 'date', 'instructor_name', 'completion_date', 'grade', 'certificate_id', 'organization_name'],
          minimumCharacters: 0
        }
      ]
    },
    menuBar: {
      isVisible: true
    },
    placeholder: 'Design your certificate template here. Use {{placeholder_name}} format for dynamic fields.',
    style: {
      definitions: [
        {
          name: 'Article category',
          element: 'h3',
          classes: ['category']
        },
        {
          name: 'Title',
          element: 'h2',
          classes: ['document-title']
        },
        {
          name: 'Subtitle',
          element: 'h3',
          classes: ['document-subtitle']
        },
        {
          name: 'Info box',
          element: 'p',
          classes: ['info-box']
        },
        {
          name: 'Side quote',
          element: 'blockquote',
          classes: ['side-quote']
        },
        {
          name: 'Marker',
          element: 'span',
          classes: ['marker']
        },
        {
          name: 'Spoiler',
          element: 'span',
          classes: ['spoiler']
        },
        {
          name: 'Code (dark)',
          element: 'pre',
          classes: ['fancy-code', 'fancy-code-dark']
        },
        {
          name: 'Code (bright)',
          element: 'pre',
          classes: ['fancy-code', 'fancy-code-bright']
        }
      ]
    },
    table: {
      contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells', 'tableProperties', 'tableCellProperties']
    }
  };

  templateForm: FormGroup;
  editorContent = '';
  extractedPlaceholders: string[] = [];
  isEditMode = false;
  templateId: number | null = null;
  isLoading = false;

  suggestedPlaceholders = [
    'student_name',
    'course_name',
    'date',
    'instructor_name',
    'completion_date',
    'grade',
    'certificate_id',
    'organization_name'
  ];

  constructor(
    private fb: FormBuilder,
    private templateService: TemplateService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.templateForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', [Validators.maxLength(1000)]],
      type: ['HTML']
    });
  }

  ngOnInit(): void {
    // Check if we're in edit mode
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.templateId = parseInt(id, 10);
        this.loadTemplate(this.templateId);
      }
    });
  }

  loadTemplate(id: number): void {
    this.isLoading = true;
    this.templateService.getTemplateById(id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          const template = response.data;
          this.templateForm.patchValue({
            name: template.name,
            description: template.description,
            type: template.type
          });
          this.editorContent = template.templateContent || '';
          this.extractPlaceholdersFromContent();
        }
        this.isLoading = false;
      },
      error: (error) => {
        this.snackBar.open('Failed to load template', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onEditorChange(event: any): void {
    this.extractPlaceholdersFromContent();
  }

  extractPlaceholdersFromContent(): void {
    const regex = /\{\{(.*?)\}\}/g;
    const matches = this.editorContent.matchAll(regex);
    const placeholders = new Set<string>();
    
    for (const match of matches) {
      const placeholder = match[1].trim();
      if (placeholder) {
        placeholders.add(placeholder);
      }
    }
    
    this.extractedPlaceholders = Array.from(placeholders);
  }

  insertPlaceholder(placeholder: string): void {
    const placeholderText = `{{${placeholder}}}`;
    // Insert at cursor position in CKEditor
    this.editorContent += ' ' + placeholderText + ' ';
    this.extractPlaceholdersFromContent();
  }

  saveTemplate(): void {
    if (this.templateForm.invalid) {
      this.snackBar.open('Please fill in all required fields', 'Close', { duration: 3000 });
      return;
    }

    if (!this.editorContent || this.editorContent.trim() === '') {
      this.snackBar.open('Please design your certificate template', 'Close', { duration: 3000 });
      return;
    }

    this.isLoading = true;

    const templateData = {
      ...this.templateForm.value,
      templateContent: this.editorContent,
      placeholders: this.extractedPlaceholders
    };

    const request = this.isEditMode && this.templateId
      ? this.templateService.updateTemplate(this.templateId, templateData)
      : this.templateService.createTemplate(templateData);

    request.subscribe({
      next: (response) => {
        this.snackBar.open(
          this.isEditMode ? 'Template updated successfully' : 'Template created successfully',
          'Close',
          { duration: 3000 }
        );
        this.isLoading = false;
        this.router.navigate(['/templates']);
      },
      error: (error) => {
        this.snackBar.open(
          error.error?.message || 'Failed to save template',
          'Close',
          { duration: 3000 }
        );
        this.isLoading = false;
      }
    });
  }

  previewTemplate(): void {
    if (!this.editorContent || this.editorContent.trim() === '') {
      this.snackBar.open('Please design your certificate template first', 'Close', { duration: 3000 });
      return;
    }

    // Navigate to simulation preview
    this.router.navigate(['/templates/simulate'], {
      state: {
        content: this.editorContent,
        placeholders: this.extractedPlaceholders,
        templateId: this.templateId
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/templates']);
  }
}
