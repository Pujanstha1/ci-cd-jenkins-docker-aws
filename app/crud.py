from sqlalchemy.orm import Session
from .models import Item
from .schemas import ItemCreate

def get_items(db: Session):
    return db.query(Item).all()

def get_item(db: Session, item_id: int):
    return db.query(Item).filter(Item.id == item_id).first()

def create_item(db: Session, item: ItemCreate):
    db_item = Item(**item.dict())
    db.add(db_item)
    db.commit()
    db.refresh(db_item)
    return db_item

def update_item(db: Session, item_id: int, item: ItemCreate):
    db_item = get_item(db, item_id)
    db_item.name = item.name
    db_item.description = item.description
    db.commit()
    return db_item

def delete_item(db: Session, item_id: int):
    item = get_item(db, item_id)
    db.delete(item)
    db.commit()
