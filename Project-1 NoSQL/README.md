# Introduction

Implement the functions provided in the Jupyter Notebook to perform the operations as listed below. You may use the included sample.db to test your functions with the Python code provided or you may use the Jupyter Notebook environment, which has the sample database embedded within.  

**A. FindBusinessBasedOnCity(cityToSearch, saveLocation1, collection)** – This function searches the ‘collection’ given to find all the business present in the city provided in ‘cityToSearch’ and save it to ‘saveLocation1’. For each business you found, you should store the name, full address, city, and state of the business in the following format. Each line of the saved file will contain: Name$FullAddress$City$State. ($ is the separator and must be present.)  

**B. FindBusinessBasedOnLocation(categoriesToSearch, myLocation, maxDistance, saveLocation2, collection)** – This function searches the ‘collection’ given to find the name of all the businesses present in the ‘maxDistance’ from the given ‘myLocation’ (refer [this](http://www.movable-type.co.uk/scripts/latlong.html) page for the distance algorithm.) and save them to ‘saveLocation2’. Each line of the output file will contain the name of the business only.  

Note: You may use the instructions in the [Updated_AdditionalTestCases.docx](Project-1 NoSQL/Updated_AdditionalTestCases.docx) file to perform additional testing.