---
name: generate-employee-csv
description: Generate a large CSV file containing realistic employee data for a company. Use this skill whenever the user asks to generate, create, or produce a CSV file with employee data, staff records, HR data, workforce data, or any employee/personnel information. Also trigger when the user needs test data, mock data, sample data, or dummy data for employee-related batch processing, testing Spring Batch jobs, or populating databases. Trigger even if the user says things like "I need some employee records", "create fake employee data", "generate test data for my HR system", or "I want a CSV with N employees".
---

# Generate Employee CSV

Generate a realistic, large CSV file containing employee data for a company.

## What this skill does

When invoked, generate a CSV file with the requested number of rows of employee data. Use the bundled Python script for efficiency — it can produce millions of rows quickly using vectorized operations.

## CSV Structure

The CSV file must include these columns in this order:

| Column | Description | Example |
|---|---|---|
| `employee_id` | Unique ID, format `EMP-XXXXXX` | `EMP-000001` |
| `first_name` | First name | `John` |
| `last_name` | Last name | `Smith` |
| `email` | Work email, format `firstname.lastname@company.com` | `john.smith@company.com` |
| `phone` | Phone number, format `+1-XXX-XXX-XXXX` | `+1-555-123-4567` |
| `department` | Department name | `Engineering` |
| `job_title` | Job title within department | `Senior Software Engineer` |
| `employment_type` | `Full-time`, `Part-time`, or `Contract` | `Full-time` |
| `hire_date` | ISO date, random between 2000-01-01 and today | `2018-06-15` |
| `salary` | Annual salary in USD (integer) | `95000` |
| `manager_id` | Manager's employee_id (or empty for top-level) | `EMP-000003` |
| `office_location` | City, State | `San Francisco, CA` |
| `status` | `Active`, `On Leave`, or `Terminated` | `Active` |
| `gender` | `Male`, `Female`, or `Non-binary` | `Female` |
| `birth_date` | ISO date (age 22–65) | `1985-03-22` |

## How to generate

Run the bundled script:

```bash
python /path/to/generate-employee-csv/scripts/generate.py \
  --rows <N> \
  --output <output_file.csv>
```

The script handles all data generation. After it finishes, confirm the output file path and row count to the user.

If the user didn't specify an output filename, default to `employees_<N>_rows.csv` in the current working directory.

## Tips

- For row counts over 1 million, warn the user it may take a moment and show progress.
- Always print the final file path and size after generation.
- If the user wants a specific field distribution (e.g., "make 80% active employees"), pass those preferences — but only adjust what's asked; keep everything else default.
- If `faker` is not installed, use the script's built-in pure-Python fallback (no external dependencies required).
