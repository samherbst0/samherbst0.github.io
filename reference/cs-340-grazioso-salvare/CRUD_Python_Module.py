# Example Python Code to Insert a Document 

from pymongo import MongoClient 
from bson.objectid import ObjectId 

class AnimalShelter(object): 
    """ CRUD operations for Animal collection in MongoDB """ 

    def __init__(self, username, password): 
        # Initializing the MongoClient. This helps to access the MongoDB 
        # databases and collections. This is hard-wired to use the aac 
        # database, the animals collection, and the aac user. 
        # 
        # You must edit the password below for your environment. 
        # 
        # Connection Variables 
        # 
        USER = 'aacuser' 
        PASS = '********' 
        HOST = 'localhost' 
        PORT = 27017 
        DB = 'aac' 
        COL = 'animals' 
        # 
        # Initialize Connection 
        # 
        self.client = MongoClient('mongodb://%s:%s@%s:%d' % (USER,PASS,HOST,PORT)) 
        self.database = self.client['%s' % (DB)] 
        self.collection = self.database['%s' % (COL)] 

    # Create a method to return the next available record number for use in the create method
            
    # Complete this create method to implement the C in CRUD. 
    def create(self, data):
        if data is not None:
            insert_result = self.database.animals.insert_one(data)  # data should be dictionary
            # Return True if insert was successful, False otherwise
            return True if insert_result.inserted_id else False
        else:
            raise Exception("Nothing to save, because data parameter is empty")

    # Create method to implement the R in CRUD.
    def read(self, query):
        if query is not None:
            # Use find() to query the collection and return results as a list
            cursor = self.database.animals.find(query)
            # Convert cursor to list and return
            return list(cursor)
        else:
            # If no query provided, return empty list
            return []
        
    # Create method to implement the U in CRUD.
    def update(self, query, update_data):
        # query should be a dictionary containing key/value lookup pairs
        # update_data should be a dictionary containing the fields to update
        if query is not None and update_data is not None:
            # Use update_many() to update all documents matching the query
            update_result = self.database.animals.update_many(query, {"$set": update_data})
            # Return the number of documents modified
            return update_result.modified_count
        else:
            # If query or update_data is empty, return 0
            return 0

    # Create method to implement the D in CRUD.
    def delete(self, query):
        # query should be a dictionary containing key/value lookup pairs
        if query is not None:
            # Use delete_many() to remove all documents matching the query
            delete_result = self.database.animals.delete_many(query)
            # Return the number of documents deleted
            return delete_result.deleted_count
        else:
            # If query is empty, return 0
            return 0