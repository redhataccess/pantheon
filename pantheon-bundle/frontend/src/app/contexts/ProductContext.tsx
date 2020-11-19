import React, { createContext, useState, useEffect } from "react";
  
export let ProductContext = createContext([]);

export interface IProductProviderProps {
  children?: React.ReactNode;
}

export function ProductProvider({ children }: IProductProviderProps) {
  const backend = "/content/products.query.json?nodeType=pant:product&orderby=name"  
  const [allProducts, setAllProducts] = useState([]);

  useEffect(() => {
    fetch(backend)
      .then(response => response.json())
      .then(response => {
        let temp: any = [];
        let key;
        let singleProduct;
        for (let i of Object.keys(response.results)) {
          key = Object.keys(response.results)[i];
          singleProduct = response.results[key];
          singleProduct = Object.assign({ "isOpen": false }, singleProduct)
          temp.push(singleProduct);
          console.log(singleProduct);
        }
        setAllProducts(temp);
      })
  }, []);

  console.log(ProductContext)
  return <ProductContext.Provider value={allProducts}>
    {children}
  </ProductContext.Provider>
}
