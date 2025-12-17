import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import PptxGenJS from 'pptxgenjs';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const rootDir = path.resolve(__dirname, '..', '..');
const backendDir = path.join(rootDir, 'backend');
const reportsDir = path.join(rootDir, 'reports');

function ensureDir(p) {
  if (!fs.existsSync(p)) fs.mkdirSync(p, { recursive: true });
}

function readTextSafe(p) {
  try {
    return fs.readFileSync(p, 'utf8');
  } catch {
    return '';
  }
}

function summarizeSurefire() {
  const dir = path.join(backendDir, 'target', 'surefire-reports');
  const summary = { tests: 0, failures: 0, errors: 0, skipped: 0, files: [] };
  if (!fs.existsSync(dir)) return summary;
  const files = fs.readdirSync(dir).filter((f) => f.toLowerCase().endsWith('.xml'));
  for (const f of files) {
    const xml = readTextSafe(path.join(dir, f));
    const m = xml.match(/<testsuite[^>]*tests="(\d+)"[^>]*failures="(\d+)"[^>]*errors="(\d+)"[^>]*skipped="(\d+)"/);
    if (m) {
      summary.tests += Number(m[1]);
      summary.failures += Number(m[2]);
      summary.errors += Number(m[3]);
      summary.skipped += Number(m[4]);
      summary.files.push(f);
    }
  }
  return summary;
}

function coverageStatus() {
  const execPath = path.join(backendDir, 'target', 'jacoco.exec');
  const htmlIndex = path.join(backendDir, 'target', 'site', 'jacoco', 'index.html');
  const csvPath = path.join(backendDir, 'target', 'site', 'jacoco', 'jacoco.csv');
  return {
    execPresent: fs.existsSync(execPath),
    htmlPresent: fs.existsSync(htmlIndex),
    csvPresent: fs.existsSync(csvPath),
    execPath,
    htmlIndex,
    csvPath
  };
}

function addTitleSlide(pptx, title, subtitle) {
  const slide = pptx.addSlide();
  slide.addText(title, { x: 0.5, y: 1.0, w: 9.0, h: 1.0, fontSize: 36, bold: true });
  if (subtitle) {
    slide.addText(subtitle, { x: 0.5, y: 2.0, w: 9.0, h: 0.6, fontSize: 18, color: '666666' });
  }
}

function addBulletsSlide(pptx, title, bullets) {
  const slide = pptx.addSlide();
  slide.addText(title, { x: 0.5, y: 0.6, w: 9.0, h: 0.8, fontSize: 28, bold: true });
  const para = bullets.map((t) => ({ text: t, options: { bullet: true, fontSize: 18, lineSpacing: 20 } }));
  slide.addText(para, { x: 0.7, y: 1.6, w: 8.6, h: 4.5 });
}

function addCodeSlide(pptx, title, text) {
  const slide = pptx.addSlide();
  slide.addText(title, { x: 0.5, y: 0.6, w: 9.0, h: 0.8, fontSize: 28, bold: true });
  slide.addText(text || '(no content found)', {
    x: 0.7,
    y: 1.6,
    w: 8.6,
    h: 4.8,
    fontFace: 'Courier New',
    fontSize: 14,
    color: '1F2937'
  });
}

function main() {
  ensureDir(reportsDir);

  const pptx = new PptxGenJS();
  pptx.author = 'Certificate Service';
  pptx.company = 'Internal';
  pptx.title = 'Testing & Coverage Report';

  const today = new Date().toLocaleDateString();
  addTitleSlide(pptx, 'Testing & Coverage Report', `Certificate Service • ${today}`);

  addBulletsSlide(pptx, 'Objectives', [
    'Stabilize tests; remove flaky dependencies',
    'Increase coverage on core logic and controllers',
    'Document approach, results, and next steps',
    'Produce a shareable PPT report'
  ]);

  const surefire = summarizeSurefire();
  const allPass = surefire.tests > 0 && surefire.failures === 0 && surefire.errors === 0;
  addBulletsSlide(pptx, 'Test Results', [
    `Test suites analyzed: ${surefire.files.length}`,
    `Total tests: ${surefire.tests}`,
    `Failures: ${surefire.failures} • Errors: ${surefire.errors} • Skipped: ${surefire.skipped}`,
    allPass ? 'Status: All tests passing' : 'Status: Check failures/errors'
  ]);

  const cov = coverageStatus();
  const covLines = [
    cov.execPresent ? `JaCoCo exec found: ${path.relative(rootDir, cov.execPath)}` : 'JaCoCo exec missing',
    cov.htmlPresent
      ? `HTML report: ${path.relative(rootDir, cov.htmlIndex)}`
      : 'HTML report not generated (CSV lock or missing report phase)',
    cov.csvPresent
      ? `CSV report: ${path.relative(rootDir, cov.csvPath)}`
      : 'CSV report not present'
  ];
  addBulletsSlide(pptx, 'Coverage Status', covLines);

  addBulletsSlide(pptx, 'Highlights', [
    'SignatureServiceTest: pure unit test (no Spring/DB)',
    'AuthController: WebMvcTest with security filters mocked/disabled',
    'CertificateController: rewritten as unit tests to avoid principal issues',
    'AuthService: login flow covered with mocks',
    'Added TEST_COVERAGE_REPORT.md and TESTING_SUMMARY.md'
  ]);

  const summaryMd = readTextSafe(path.join(rootDir, 'TESTING_SUMMARY.md'));
  const coverageMd = readTextSafe(path.join(rootDir, 'TEST_COVERAGE_REPORT.md'));
  const summaryExcerpt = summaryMd.split('\n').slice(0, 30).join('\n');
  const coverageExcerpt = coverageMd.split('\n').slice(0, 30).join('\n');
  addCodeSlide(pptx, 'Testing Summary (excerpt)', summaryExcerpt);
  addCodeSlide(pptx, 'Coverage Report (excerpt)', coverageExcerpt);

  addBulletsSlide(pptx, 'Next Steps', [
    'Stabilize JaCoCo HTML/CSV generation on Windows',
    'Integrate tests and coverage into CI pipeline',
    'Add more service-level tests for edge cases',
    'Optionally add integration tests for secured endpoints'
  ]);

  const outPath = path.join(reportsDir, 'Testing_and_Coverage_Report.pptx');
  pptx.writeFile({ fileName: outPath }).then(() => {
    console.log(`PPT generated: ${outPath}`);
  });
}

main();
