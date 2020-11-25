import React, { createContext, useState, useEffect } from "react";

export let ProductContext = createContext<IProduct[]>([] as IProduct[]);

export interface IProductProviderProps {
  children?: React.ReactNode;
}

export interface IProduct {
  description: string,
  isOpen: boolean,
  ["jcr:created"]: number,
  ["jcr:createdBy"]: string,
  ['jcr:lastModified']: number,
  ["jcr:lastModifiedBy"]: string,
  ["jcr:primaryType"]: string,
  ["jcr:uuid"]: string,
  locale: string
  name: string,
  ['sling:resourceType']: string,
  urlFragment: string,
}

export function ProductProvider({ children }: IProductProviderProps) {
  const backend = "/content/products.query.json?nodeType=pant:product&orderby=name"
  const [allProducts, setAllProducts]: any = useState([] as IProduct[]);

  useEffect(() => {
    try {
      fetch(backend)
        .then(response => response.json())
        .then(response => {
          response.results.forEach(product => {
            product.isOpen = false
          })
          setAllProducts(response.results);
        }).catch(e => console.error(e));
    } catch {
      console.error("unexpected error");
    }

  }, []);

  return <ProductContext.Provider value={allProducts}>
    {children}
  </ProductContext.Provider>
}
