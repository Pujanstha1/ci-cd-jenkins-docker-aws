from fastapi import FastAPI, Request, Form
from fastapi.templating import Jinja2Templates
from fastapi.responses import RedirectResponse
from .database import SessionLocal, engine
from . import models, crud, schemas
from fastapi.staticfiles import StaticFiles

models.Base.metadata.create_all(bind=engine)

app = FastAPI()

# Mount static folder
app.mount("/static", StaticFiles(directory="app/static"), name="static")

templates = Jinja2Templates(directory="app/templates")

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.get("/")
def read_items(request: Request):
    db = SessionLocal()
    items = crud.get_items(db)
    return templates.TemplateResponse("index.html", {"request": request, "items": items})

@app.get("/create")
def create_page(request: Request):
    return templates.TemplateResponse("create.html", {"request": request})

@app.post("/create")
def create_item(name: str = Form(...), description: str = Form(...)):
    db = SessionLocal()
    crud.create_item(db, schemas.ItemCreate(name=name, description=description))
    return RedirectResponse("/", status_code=303)

@app.get("/edit/{item_id}")
def edit_page(item_id: int, request: Request):
    db = SessionLocal()
    item = crud.get_item(db, item_id)
    return templates.TemplateResponse("edit.html", {"request": request, "item": item})

@app.post("/edit/{item_id}")
def edit_item(item_id: int, name: str = Form(...), description: str = Form(...)):
    db = SessionLocal()
    crud.update_item(db, item_id, schemas.ItemCreate(name=name, description=description))
    return RedirectResponse("/", status_code=303)

@app.get("/delete/{item_id}")
def delete_item(item_id: int):
    db = SessionLocal()
    crud.delete_item(db, item_id)
    return RedirectResponse("/", status_code=303)
