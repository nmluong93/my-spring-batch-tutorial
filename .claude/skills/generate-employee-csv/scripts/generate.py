#!/usr/bin/env python3
"""Generate a large CSV file of realistic employee data."""

import argparse
import csv
import os
import random
import sys
from datetime import date, timedelta

FIRST_NAMES = [
    "James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda",
    "William", "Barbara", "David", "Elizabeth", "Richard", "Susan", "Joseph", "Jessica",
    "Thomas", "Sarah", "Charles", "Karen", "Christopher", "Lisa", "Daniel", "Nancy",
    "Matthew", "Betty", "Anthony", "Margaret", "Mark", "Sandra", "Donald", "Ashley",
    "Steven", "Dorothy", "Paul", "Kimberly", "Andrew", "Emily", "Kenneth", "Donna",
    "Joshua", "Michelle", "Kevin", "Carol", "Brian", "Amanda", "George", "Melissa",
    "Timothy", "Deborah", "Ronald", "Stephanie", "Edward", "Rebecca", "Jason", "Sharon",
    "Jeffrey", "Laura", "Ryan", "Cynthia", "Jacob", "Kathleen", "Gary", "Amy",
    "Nicholas", "Angela", "Eric", "Shirley", "Jonathan", "Anna", "Stephen", "Brenda",
    "Larry", "Pamela", "Justin", "Emma", "Scott", "Nicole", "Brandon", "Helen",
    "Benjamin", "Samantha", "Samuel", "Katherine", "Raymond", "Christine", "Gregory",
    "Debra", "Frank", "Rachel", "Alexander", "Carolyn", "Patrick", "Janet", "Jack",
    "Catherine", "Dennis", "Maria", "Jerry", "Heather", "Tyler", "Diane", "Aaron",
    "Olivia", "Jose", "Julie", "Adam", "Joyce", "Henry", "Victoria", "Nathan", "Ruth",
    "Zachary", "Virginia", "Douglas", "Lauren", "Peter", "Kelly", "Kyle", "Christina",
    "Noah", "Joan", "Ethan", "Evelyn", "Jeremy", "Judith", "Walter", "Andrea", "Christian",
    "Megan", "Keith", "Alice", "Roger", "Jean", "Terry", "Cheryl", "Austin", "Marie",
    "Sean", "Madison", "Gerald", "Teresa", "Carl", "Kathryn", "Dylan", "Gloria",
    "Harold", "Frances", "Jordan", "Ann", "Jesse", "Martha", "Bryan", "Sara",
    "Lawrence", "Jacqueline", "Arthur", "Grace", "Gabriel", "Amber", "Bruce", "Hannah",
    "Logan", "Theresa", "Billy", "Denise", "Joe", "Marilyn", "Alan", "Danielle",
    "Juan", "Diana", "Elijah", "Beverly", "Willie", "Isabella", "Albert", "Natalie",
    "Wayne", "Sophia", "Randy", "Rose", "Mason", "Alexis", "Vincent", "Alice",
    "Liam", "Abigail", "Lucas", "Lily", "Harper", "Charlotte", "Aiden", "Ella",
]

LAST_NAMES = [
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
    "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
    "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
    "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker",
    "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
    "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell",
    "Carter", "Roberts", "Gomez", "Phillips", "Evans", "Turner", "Diaz", "Parker",
    "Cruz", "Edwards", "Collins", "Reyes", "Stewart", "Morris", "Morales", "Murphy",
    "Cook", "Rogers", "Gutierrez", "Ortiz", "Morgan", "Cooper", "Peterson", "Bailey",
    "Reed", "Kelly", "Howard", "Ramos", "Kim", "Cox", "Ward", "Richardson", "Watson",
    "Brooks", "Chavez", "Wood", "James", "Bennett", "Gray", "Mendoza", "Ruiz",
    "Hughes", "Price", "Alvarez", "Castillo", "Sanders", "Patel", "Myers", "Long",
    "Ross", "Foster", "Jimenez", "Powell", "Jenkins", "Perry", "Russell", "Sullivan",
    "Bell", "Coleman", "Butler", "Henderson", "Barnes", "Gonzales", "Fisher", "Vasquez",
    "Simmons", "Romero", "Jordan", "Patterson", "Alexander", "Hamilton", "Graham",
    "Reynolds", "Griffin", "Wallace", "Moreno", "West", "Cole", "Hayes", "Bryant",
    "Herrera", "Gibson", "Ellis", "Tran", "Medina", "Aguilar", "Stevens", "Murray",
    "Ford", "Castro", "Marshall", "Owens", "Harrison", "Fernandez", "McDonald",
    "Woods", "Washington", "Kennedy", "Wells", "Vargas", "Henry", "Chen", "Freeman",
    "Webb", "Tucker", "Guzman", "Burns", "Crawford", "Olson", "Simpson", "Porter",
    "Hunter", "Gordon", "Mendez", "Silva", "Shaw", "Snyder", "Mason", "Dixon",
]

DEPARTMENTS = {
    "Engineering": [
        "Software Engineer", "Senior Software Engineer", "Principal Engineer",
        "Staff Engineer", "Engineering Manager", "QA Engineer", "DevOps Engineer",
        "Data Engineer", "Site Reliability Engineer", "Frontend Engineer",
    ],
    "Product": [
        "Product Manager", "Senior Product Manager", "Director of Product",
        "Product Analyst", "Technical Product Manager",
    ],
    "Design": [
        "UX Designer", "Senior UX Designer", "UI Designer", "Product Designer",
        "Visual Designer", "Design Lead",
    ],
    "Marketing": [
        "Marketing Manager", "Content Strategist", "SEO Specialist", "Brand Manager",
        "Digital Marketing Specialist", "Growth Manager", "Marketing Analyst",
    ],
    "Sales": [
        "Sales Representative", "Account Executive", "Senior Account Executive",
        "Sales Manager", "Business Development Rep", "Sales Director",
    ],
    "Finance": [
        "Financial Analyst", "Senior Financial Analyst", "Accountant", "Controller",
        "Finance Manager", "CFO", "Budget Analyst", "Treasury Analyst",
    ],
    "Human Resources": [
        "HR Generalist", "HR Business Partner", "Recruiter", "Talent Acquisition Specialist",
        "Compensation Analyst", "HR Manager", "CHRO", "People Operations Manager",
    ],
    "Legal": [
        "Legal Counsel", "Senior Legal Counsel", "Paralegal", "Compliance Manager",
        "Contract Manager", "General Counsel",
    ],
    "Operations": [
        "Operations Manager", "Operations Analyst", "Business Analyst",
        "Project Manager", "Program Manager", "Supply Chain Manager",
    ],
    "Customer Success": [
        "Customer Success Manager", "Customer Support Specialist", "Account Manager",
        "Customer Success Director", "Support Engineer",
    ],
    "Data Science": [
        "Data Scientist", "Senior Data Scientist", "ML Engineer", "Data Analyst",
        "Research Scientist", "Analytics Engineer",
    ],
    "Security": [
        "Security Engineer", "Information Security Analyst", "Security Architect",
        "Penetration Tester", "CISO", "Security Operations Analyst",
    ],
}

OFFICE_LOCATIONS = [
    "San Francisco, CA", "New York, NY", "Austin, TX", "Seattle, WA", "Chicago, IL",
    "Boston, MA", "Los Angeles, CA", "Denver, CO", "Atlanta, GA", "Miami, FL",
    "Dallas, TX", "Portland, OR", "Minneapolis, MN", "Phoenix, AZ", "San Diego, CA",
    "Nashville, TN", "Raleigh, NC", "Detroit, MI", "Philadelphia, PA", "Washington, DC",
    "Remote", "Remote", "Remote",  # weighted to appear more often
]

EMPLOYMENT_TYPES = ["Full-time", "Full-time", "Full-time", "Part-time", "Contract"]
STATUSES = ["Active", "Active", "Active", "Active", "On Leave", "Terminated"]
GENDERS = ["Male", "Female", "Female", "Male", "Non-binary"]

SALARY_RANGES = {
    "Engineering": (70000, 250000),
    "Product": (80000, 220000),
    "Design": (65000, 180000),
    "Marketing": (55000, 150000),
    "Sales": (50000, 160000),
    "Finance": (60000, 200000),
    "Human Resources": (55000, 150000),
    "Legal": (80000, 280000),
    "Operations": (55000, 160000),
    "Customer Success": (50000, 130000),
    "Data Science": (85000, 260000),
    "Security": (80000, 240000),
}


def random_date(start: date, end: date) -> date:
    return start + timedelta(days=random.randint(0, (end - start).days))


def generate_employees(n: int, output_path: str) -> None:
    today = date.today()
    start_hire = date(2000, 1, 1)
    min_birth = date(today.year - 65, today.month, today.day)
    max_birth = date(today.year - 22, today.month, today.day)

    departments = list(DEPARTMENTS.keys())

    # Pre-build a small pool of manager IDs (top 5% of employees)
    manager_pool_size = max(1, n // 20)
    manager_ids: list[str] = []

    # Track email uniqueness
    email_counts: dict[str, int] = {}

    show_progress = n >= 100_000

    with open(output_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow([
            "employee_id", "first_name", "last_name", "email", "phone",
            "department", "job_title", "employment_type", "hire_date", "salary",
            "manager_id", "office_location", "status", "gender", "birth_date",
        ])

        for i in range(1, n + 1):
            emp_id = f"EMP-{i:06d}"

            first = random.choice(FIRST_NAMES)
            last = random.choice(LAST_NAMES)

            base_email = f"{first.lower()}.{last.lower()}@company.com"
            count = email_counts.get(base_email, 0) + 1
            email_counts[base_email] = count
            email = base_email if count == 1 else f"{first.lower()}.{last.lower()}{count}@company.com"

            phone = f"+1-{random.randint(200,999)}-{random.randint(100,999)}-{random.randint(1000,9999)}"
            dept = random.choice(departments)
            title = random.choice(DEPARTMENTS[dept])
            emp_type = random.choice(EMPLOYMENT_TYPES)
            hire_date = random_date(start_hire, today)
            lo, hi = SALARY_RANGES[dept]
            salary = random.randint(lo // 1000, hi // 1000) * 1000
            gender = random.choice(GENDERS)
            birth_date = random_date(min_birth, max_birth)
            location = random.choice(OFFICE_LOCATIONS)
            status = random.choice(STATUSES)

            # Assign manager: use pool if populated, else empty
            if i <= manager_pool_size:
                manager_id = ""
                manager_ids.append(emp_id)
            else:
                manager_id = random.choice(manager_ids)

            writer.writerow([
                emp_id, first, last, email, phone, dept, title,
                emp_type, hire_date.isoformat(), salary,
                manager_id, location, status, gender, birth_date.isoformat(),
            ])

            if show_progress and i % 100_000 == 0:
                print(f"  {i:,} / {n:,} rows written...", flush=True)

    size_bytes = os.path.getsize(output_path)
    size_mb = size_bytes / (1024 * 1024)
    print(f"Done! {n:,} rows written to: {output_path} ({size_mb:.1f} MB)")


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate employee CSV data")
    parser.add_argument("--rows", type=int, required=True, help="Number of employee rows to generate")
    parser.add_argument("--output", type=str, default=None, help="Output CSV file path")
    args = parser.parse_args()

    if args.rows <= 0:
        print("Error: --rows must be a positive integer", file=sys.stderr)
        sys.exit(1)

    output = args.output or f"employees_{args.rows}_rows.csv"

    print(f"Generating {args.rows:,} employee records -> {output}")
    if args.rows >= 1_000_000:
        print("Large file: this may take a minute...")

    generate_employees(args.rows, output)


if __name__ == "__main__":
    main()
